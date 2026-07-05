# System Architecture

## Domain Model

```
Account (loginId, passwordHash)
    │
    ├── Parent (name, ...)  ← one or more per account
    │       │
    │       └── Session (tied to a specific parent)
    │
    └── Student (name, gender, birthYear, class)  ← multiple per account
```

## Layer Overview

```
┌────────────────────────────────────────────────────────────┐
│                     Client Layer                            │
│   Mobile App (Android/iOS)    Web App (React/Angular)       │
└────────────────────────────┬───────────────────────────────┘
                             │ HTTP REST
                             ▼
┌────────────────────────────────────────────────────────────┐
│                   API Gateway (Port 8080)                   │
│   ┌──────────────┐  ┌──────────────────────────┐           │
│   │ CORS Filter   │  │  Security Filter Chain   │           │
│   └──────────────┘  └──────────────────────────┘           │
└────────────────────────────┬───────────────────────────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
┌─────────────────────┐ ┌──────────┐ ┌──────────────┐
│   AuthController    │ │ParentCtlr│ │StudentCtlr   │
│   /api/auth/*       │ │/api/parents │ /api/students│
└──────────┬──────────┘ └────┬─────┘ └──────┬───────┘
           │                 │              │
           ▼                 ▼              ▼
┌────────────────────────────────────────────────────────────┐
│                   Business Logic Layer                      │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│   │AccountService│  │ParentService │  │StudentService│    │
│   └──────┬───────┘  └──────────────┘  └──────────────┘    │
│          │                                                 │
│   ┌──────┴──────────────┐                                  │
│   │   PasswordUtil      │  ← BCrypt (10 rounds)            │
│   └─────────────────────┘                                  │
│                                                             │
│   ┌─────────────────────────────────────┐                   │
│   │ GlobalExceptionHandler (@ControllerAdvice)              │
│   └─────────────────────────────────────┘                   │
└────────────────────────────┬───────────────────────────────┘
                             │ JPA / Hibernate
                             ▼
┌────────────────────────────────────────────────────────────┐
│                     Data Layer                              │
│   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│   │ accounts │  │ parents  │  │ students │  │ sessions │ │
│   └──────────┘  └──────────┘  └──────────┘  └──────────┘ │
│              H2 (dev) / PostgreSQL (prod)                  │
└────────────────────────────────────────────────────────────┘
```

## Modules

- **api** — Spring Boot 3.3.5 application, all source code under `com.studyshield.user.*`
- **qa** — Quality assurance module (future)

## Request Flow

1. Client sends HTTP request to any API endpoint
2. `CORS Filter` handles cross-origin requests (all origins allowed)
3. `Security Filter` applies security headers (`X-Content-Type-Options`, `X-Frame-Options`)
4. Spring Security permits `/api/auth/**`, `/api/parents/**`, `/api/students/**`, `/api/config/**`, Swagger UI, H2 console
5. The controller receives the request and delegates to the appropriate service
6. Auth-protected endpoints extract Bearer token from `Authorization` header and validate the session manually
7. Service performs business logic using JPA repositories
8. `PasswordUtil` handles BCrypt hashing/verification (10 salt rounds)
9. `GlobalExceptionHandler` catches exceptions and returns consistent JSON error responses

## Data Flow by Endpoint

### POST /api/auth/signup
```
Client → AuthController → AccountService.registerUser()
  → PasswordUtil.encodePassword() → BCrypt 10 rounds
  → AccountRepository.save() → INSERT INTO accounts
  → ParentRepository.save() → INSERT INTO parents (auto-generated name if omitted)
  → SessionRepository.save() → INSERT INTO sessions (30 day expiry)
  ← 201 { accountId, parentId, parentName, sessionId, loginId }
```

### POST /api/auth/signin
```
Client → AuthController → AccountService.authenticateUser()
  → AccountRepository.findByLoginId()
  → PasswordUtil.matches() → BCrypt compare
  → If 1 parent: auto-select + create session
  → If 2+ parents + no parentId: return parent list
  → If parentId provided: validate + create session for that parent
  ← 200 { sessionId, parentId, parentName } OR { requiresParentSelection, parents[] }
  ← 401 if credentials wrong
```

### POST /api/auth/signout
```
Client → AuthController → AccountService.signOut()
  → SessionRepository.findBySessionIdAndIsActiveTrue()
  → session.isActive = false → SessionRepository.save()
  ← 200 { message }
```

### POST /api/auth/validate
```
Client → AuthController → AccountService.validateSession()
  → SessionRepository.findBySessionIdAndIsActiveTrueAndExpiresAtAfter()
  → AccountRepository.findById()
  → ParentRepository.findById()
  ← 200 { accountId, loginId, parentId, parentName, isValid: true }
  ← 401 if invalid
```

### POST /api/parents (auth required)
```
Client → ParentController → ParentService.addParent()
  → SessionRepository.findBySessionIdAndIsActiveTrue() [validate session]
  → ParentRepository.save() → INSERT INTO parents
  ← 201 { parentId, accountId, name }
```

### GET /api/students (auth required)
```
Client → StudentController → StudentService.listStudents()
  → SessionRepository.findBySessionIdAndIsActiveTrue() [validate session]
  → StudentRepository.findByAccountId() → SELECT from students
  ← 200 [{ studentId, name, gender, birthYear, studentClass }]
```

## Database Schema

### accounts
| Column | Type | Notes |
|--------|------|-------|
| account_id | UUID | PK |
| login_id | VARCHAR(255) | UNIQUE, email or phone |
| password_hash | VARCHAR(255) | BCrypt hash, 10 rounds |
| created_at | BIGINT | epoch millis |
| updated_at | BIGINT | epoch millis |

### parents
| Column | Type | Notes |
|--------|------|-------|
| parent_id | UUID | PK |
| account_id | UUID | FK → accounts |
| name | VARCHAR(100) | auto-generated if omitted |
| created_at | BIGINT | |
| updated_at | BIGINT | |

### students
| Column | Type | Notes |
|--------|------|-------|
| student_id | UUID | PK |
| account_id | UUID | FK → accounts |
| name | VARCHAR(100) | NOT NULL |
| gender | VARCHAR(10) | MALE / FEMALE / OTHER |
| birth_year | INT | |
| class | VARCHAR(50) | "Nursery", "Junior KG", etc. |
| created_at | BIGINT | |
| updated_at | BIGINT | |

### sessions
| Column | Type | Notes |
|--------|------|-------|
| session_id | UUID | PK |
| account_id | UUID | FK → accounts |
| parent_id | UUID | FK → parents |
| is_active | BOOLEAN | soft-delete flag, default true |
| created_at | BIGINT | |
| expires_at | BIGINT | epoch millis, 30 days from now |

## Key Design Decisions

- **Account + Parent split** — `loginId` lives on `Account` (shared by multiple parents). Parent profiles are separate entities with their own IDs.
- **Auto-generated parent names** — `awesome-parent-{6char}` when name is omitted, changeable later via `PUT /api/parents/me`
- **Two-phase signin** — When multiple parents exist, returns parent list without creating a session. Client retries with `parentId` to complete signin.
- **Session tied to Parent** — Each session records which parent authenticated, enabling per-parent identification.
- **Soft-delete for sessions** — `isActive = false` instead of hard delete
- **Generic 401** — Never reveals whether loginId exists
- **Manual UUIDs** — No `@GeneratedValue` on sessionId/parentId/studentId to avoid Hibernate overriding
- **Long timestamps** — Epoch millis instead of SQL TIMESTAMP
- **Manual Bearer auth** — Bearer tokens validated in controller/service layer (not Spring Security)
- **Static class config** — `class-config.json` loaded at runtime, editable without code changes

## Security

- BCrypt with 10 salt rounds for password hashing
- Spring Security permits all API endpoints (auth is manual via Bearer token validation in the service layer)
- CSRF disabled (stateless API)
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- CORS allows all origins (`*`)
