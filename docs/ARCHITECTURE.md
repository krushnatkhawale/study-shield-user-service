# System Architecture

## Layer Overview

```
┌─────────────────────────────────────┐
│         Client Layer                │
│  Mobile App (Android/iOS)           │
│  Web App (React/Angular)            │
└──────────────┬──────────────────────┘
               │ HTTP REST (/api/auth/*)
               ▼
┌─────────────────────────────────────┐
│         API Gateway                 │
│  ┌─────────────────────────────┐    │
│  │  Spring Boot API (port 8080)│    │
│  │  CORS Filter                │    │
│  │  Security Filter            │    │
│  └──────────┬──────────────────┘    │
└──────────────┼──────────────────────┘
               │ @RestController
               ▼
┌─────────────────────────────────────┐
│         Business Logic              │
│  ┌─────────────────────────────┐    │
│  │  UserController             │    │
│  │  UserService                │    │
│  │  PasswordUtil (BCrypt)      │    │
│  │  GlobalExceptionHandler     │    │
│  └──────┬──────────────────────┘    │
└─────────┼───────────────────────────┘
          │ JPA / Hibernate
          ▼
┌─────────────────────────────────────┐
│         Data Layer                  │
│  ┌─────────────────────────────┐    │
│  │  H2 / PostgreSQL (database) │    │
│  │  ├── users table            │    │
│  │  └── sessions table         │    │
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

## Modules

- **api** — Spring Boot 3.3.5 application, all source code under `com.studyshield.user.*`
- **qa** — Quality assurance module (future)

## Request Flow

1. Client sends HTTP request to `/api/auth/*`
2. `CORS Filter` handles cross-origin requests (all origins allowed)
3. `Security Filter` applies security headers (X-Content-Type-Options, X-Frame-Options, etc.)
4. Spring Security permits all requests to `/api/auth/**` (no authentication required for auth endpoints)
5. `UserController` receives the request and delegates to `UserService`
6. `UserService` performs business logic (register, authenticate, sign out, validate)
7. `PasswordUtil` handles BCrypt hashing/verification (10 salt rounds)
8. `UserRepository` / `SessionRepository` persist/query data via JPA/Hibernate
9. `GlobalExceptionHandler` catches exceptions and returns consistent JSON error responses

## Data Flow by Endpoint

### POST /api/auth/signup
```
Client → signup(loginId, password) → UserService.registerUser()
  → PasswordUtil.hashPassword() → BCrypt 10 rounds
  → UserRepository.save() → INSERT INTO users
  → SessionRepository.save() → INSERT INTO sessions (30 day expiry)
  ← 201 { userId, sessionId, loginId }
```

### POST /api/auth/signin
```
Client → signin(loginId, password) → UserService.authenticateUser()
  → UserRepository.findByLoginId()
  → PasswordUtil.verifyPassword() → BCrypt compare
  → SessionRepository.save() → INSERT INTO sessions (30 day expiry)
  ← 200 { userId, sessionId, loginId }
  ← 401 { error } if loginId not found OR password wrong
```

### POST /api/auth/signout
```
Client → signout(sessionId) → UserService.signOut()
  → SessionRepository.findBySessionIdAndIsActiveTrue()
  → session.isActive = false → SessionRepository.save()
  ← 200 { userId, sessionId, loginId }
  ← 401 if session not found or inactive
```

### POST /api/auth/validate
```
Client → validate(sessionId) → UserService.validateSession()
  → SessionRepository.findBySessionIdAndIsActiveTrueAndExpiresAtAfter()
  ← 200 { userId, loginId, valid: true }
  ← 401 { valid: false, error }
```

## Database Schema

### users
| Column       | Type         | Notes                    |
|-------------|--------------|--------------------------|
| user_id     | VARCHAR(36)  | UUID, PK                 |
| login_id    | VARCHAR(100) | UNIQUE, email or phone   |
| password_hash| VARCHAR(255)| BCrypt hash, 10 rounds   |
| created_at  | BIGINT       | epoch millis             |
| updated_at  | BIGINT       | epoch millis             |

### sessions
| Column      | Type         | Notes                          |
|-------------|--------------|--------------------------------|
| session_id  | VARCHAR(36)  | UUID, PK                       |
| user_id     | VARCHAR(36)  | FK → users.user_id             |
| is_active   | BOOLEAN      | soft-delete flag               |
| expires_at  | BIGINT       | epoch millis, 30 days from now |

## Key Design Decisions

- **loginId** — Single field for email or phone (not separate columns)
- **Soft-delete for sessions** — `isActive = false` instead of hard delete
- **Generic 401** — Never reveals whether loginId exists
- **Manual UUIDs** — No `@GeneratedValue` on sessionId to avoid Hibernate overriding
- **Long timestamps** — Epoch millis instead of SQL TIMESTAMP (simpler, no timezone issues)
- **Main class** in `com.studyshield` — Sub-packages scanned automatically by Spring Boot

## Security

- BCrypt with 10 salt rounds for password hashing
- Spring Security permits `/api/auth/**` unauthenticated (these ARE the auth endpoints)
- CSRF disabled (stateless API)
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- CORS allows all origins (`*`)
