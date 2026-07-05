# Study Shield User Service — Documentation

> A Spring Boot REST API for family account management with parent profiles,
> student profiles, and session-based authentication.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [API Endpoints](#api-endpoints)
   - [Auth](#auth-endpoints)
   - [Parents](#parent-endpoints)
   - [Students](#student-endpoints)
   - [Config](#config-endpoints)
3. [Flow Diagrams](#flow-diagrams)
4. [Database Schema](#database-schema)
5. [Error Codes](#error-codes)
6. [Configuration](#configuration)
7. [Running the Application](#running-the-application)

---

## Architecture Overview

### Layer Diagram

```
┌────────────────────────────────────────────────────────────┐
│                     Client Layer                            │
│   Mobile App (Android/iOS)    Web App (React/Angular)       │
└────────────────────────────┬───────────────────────────────┘
                             │ HTTP REST (JSON)
                             ▼
┌────────────────────────────────────────────────────────────┐
│                   API Gateway (Port 8080)                   │
│   ┌──────────────┐  ┌──────────────────────────┐           │
│   │ CORS Filter   │  │  Security Filter Chain   │           │
│   └──────────────┘  └──────────────────────────┘           │
└────────────────────────────┬───────────────────────────────┘
                             │ @RestController
                             ▼
┌────────────────────────────────────────────────────────────┐
│                   Business Logic Layer                      │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│   │AuthController│  │ParentCntrlr  │  │StudentCntrlr │    │
│   └──────┬───────┘  └──────┬───────┘  └──────┬───────┘    │
│          │                 │                 │             │
│   ┌──────┴─────────────────┴─────────────────┴───────┐    │
│   │         AccountService                            │    │
│   │         ParentService                             │    │
│   │         StudentService                            │    │
│   └──────┬────────────────────────────────────────────┘    │
│          │                                                 │
│   ┌──────┴──────────────┐                                  │
│   │   PasswordUtil      │  ← BCrypt (10 rounds)            │
│   └─────────────────────┘                                  │
│                                                             │
│   ┌─────────────────────┐                                  │
│   │GlobalExceptionHdlr  │  ← @ControllerAdvice             │
│   └─────────────────────┘                                  │
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

### Key Components

| Component | Responsibility |
|-----------|---------------|
| `AuthController` | Signup, signin, signout, session validation |
| `ParentController` | List, add, rename parents |
| `StudentController` | CRUD for student profiles |
| `AccountService` | Account auth + session management |
| `ParentService` | Parent profile business logic |
| `StudentService` | Student profile business logic |
| `PasswordUtil` | BCrypt encoding & verification |
| `GlobalExceptionHandler` | Centralized error handling (`@ControllerAdvice`) |
| `WebConfiguration` | CORS + security headers |
| `SecurityConfig` | Spring Security (CSRF off, permit API endpoints) |

---

## API Endpoints

### Auth Endpoints

#### `POST /api/auth/signup` — Register Account + Primary Parent

**Request:**
```json
{
  "loginId": "user@example.com",
  "password": "SecurePass123",
  "name": "Alice"
}
```
`name` is optional. If omitted, auto-generated as `awesome-parent-{6chars}`.

**Success (201):**
```json
{
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "loginId": "user@example.com",
  "sessionId": "660e8400-e29b-41d4-a716-446655440001",
  "parentId": "770e8400-e29b-41d4-a716-446655440002",
  "parentName": "Alice",
  "requiresParentSelection": false,
  "message": "Sign up successful",
  "errorCode": null,
  "timestamp": 1719907200000
}
```

**Errors:** `400` (invalid format), `409` (duplicate loginId), `500` (internal)

---

#### `POST /api/auth/signin` — Authenticate & Get Session

**Request without parentId (auto-select or show selection):**
```json
{
  "loginId": "user@example.com",
  "password": "SecurePass123"
}
```

**Request with parentId (select specific parent):**
```json
{
  "loginId": "user@example.com",
  "password": "SecurePass123",
  "parentId": "770e8400-e29b-41d4-a716-446655440002"
}
```

**Success (200) — auto-selected (1 parent):**
```json
{
  "accountId": "550e8400-...",
  "sessionId": "660e8400-...",
  "parentId": "770e8400-...",
  "parentName": "Alice",
  "requiresParentSelection": false,
  "message": "Sign in successful",
  "errorCode": null,
  "timestamp": 1719907200000
}
```

**Success (200) — parent selection required (2+ parents, no parentId sent):**
```json
{
  "requiresParentSelection": true,
  "parents": [
    { "parentId": "770e8400-...", "parentName": "Alice" },
    { "parentId": "880e8400-...", "parentName": "Bob" }
  ],
  "message": "Multiple parents found. Please select a parent.",
  "errorCode": null,
  "timestamp": 1719907200000
}
```

**Errors:** `400` (missing fields), `401` (invalid credentials — generic message)

---

#### `POST /api/auth/signout` — Invalidate Session

**Request (header):**
```
Authorization: Bearer 660e8400-e29b-41d4-a716-446655440001
```

**Request (body):**
```json
{ "sessionId": "660e8400-e29b-41d4-a716-446655440001" }
```

**Success (200):**
```json
{
  "message": "Sign out successful",
  "errorCode": null,
  "timestamp": 1719907200000
}
```

**Errors:** `400` (missing sessionId), `401` (invalid/expired session)

---

#### `POST /api/auth/validate` — Validate Session

**Request:**
```
Authorization: Bearer 660e8400-e29b-41d4-a716-446655440001
```

**Success (200):**
```json
{
  "accountId": "550e8400-...",
  "loginId": "user@example.com",
  "parentId": "770e8400-...",
  "parentName": "Alice",
  "isValid": true,
  "message": "Session valid",
  "errorCode": null,
  "timestamp": 1719907200000
}
```

**Errors:** `400` (malformed header), `401` (expired/invalid session)

---

### Parent Endpoints

All require `Authorization: Bearer {sessionId}` header.

#### `POST /api/parents` — Add Another Parent

```json
{ "name": "Bob" }
```

`name` is optional. If omitted, auto-generated.

**Success (201):**
```json
{
  "parentId": "990e8400-...",
  "accountId": "550e8400-...",
  "name": "Bob"
}
```

---

#### `GET /api/parents` — List Parents

**Success (200):**
```json
[
  { "parentId": "770e8400-...", "accountId": "550e8400-...", "name": "Alice" },
  { "parentId": "990e8400-...", "accountId": "550e8400-...", "name": "Bob" }
]
```

---

#### `PUT /api/parents/me` — Update Own Name

```json
{ "name": "Alice Updated" }
```

**Success (200):**
```json
{
  "parentId": "770e8400-...",
  "accountId": "550e8400-...",
  "name": "Alice Updated"
}
```

---

### Student Endpoints

All require `Authorization: Bearer {sessionId}` header.

#### `POST /api/students` — Add a Student

```json
{
  "name": "Charlie",
  "gender": "MALE",
  "birthYear": 2018,
  "studentClass": "1st"
}
```

**Success (201):**
```json
{
  "studentId": "aa0e8400-...",
  "accountId": "550e8400-...",
  "name": "Charlie",
  "gender": "MALE",
  "birthYear": 2018,
  "studentClass": "1st"
}
```

---

#### `GET /api/students` — List Students

**Success (200):**
```json
[
  {
    "studentId": "aa0e8400-...",
    "accountId": "550e8400-...",
    "name": "Charlie",
    "gender": "MALE",
    "birthYear": 2018,
    "studentClass": "1st"
  }
]
```

---

#### `PUT /api/students/{id}` — Update Student

```json
{
  "name": "Charlie Updated",
  "gender": "MALE",
  "birthYear": 2019,
  "studentClass": "2nd"
}
```

**Errors:** `404` (student not found), `401` (unauthorized access)

---

#### `DELETE /api/students/{id}` — Remove Student

**Success:** `204 No Content`

---

### Config Endpoints

#### `GET /api/config/classes` — Get Class List

No auth required.

**Success (200):**
```json
{
  "default": {
    "name": "Default",
    "classes": [
      "Nursery", "Junior KG", "Senior KG",
      "1st", "2nd", "3rd", "4th", "5th",
      "6th", "7th", "8th", "9th", "10th",
      "11th", "12th"
    ]
  }
}
```

---

## Flow Diagrams

PlantUML source files are available in the [`docs/`](./) directory.

### 1. Sign-Up Flow

```
Client → POST /api/auth/signup {loginId, password, name?}
  → AccountService.registerUser()
    → AccountRepository.findByLoginId()  [check duplicate]
    → PasswordUtil.encodePassword()      [BCrypt 10 rounds]
    → AccountRepository.save()           [INSERT INTO accounts]
    → ParentRepository.save()            [INSERT INTO parents, auto-generated name if omitted]
    → SessionRepository.save()           [INSERT INTO sessions, 30 day expiry]
  ← 201 { accountId, loginId, sessionId, parentId, parentName }
```

### 2. Sign-In Flow

```
Client → POST /api/auth/signin {loginId, password, parentId?}
  → AccountService.authenticateUser()
    → AccountRepository.findByLoginId()
    → PasswordUtil.matches()             [BCrypt compare]
    → If parentId provided:
        → Verify parent belongs to account
        → Create session for that parent
    → If no parentId:
        → Count parents in account
        → If 1 parent: auto-select, create session
        → If 2+ parents: return parent list (requiresParentSelection: true)
  ← 200 { sessionId, parentId, parentName } OR { requiresParentSelection, parents[] }
  ← 401 { error } if credentials wrong
```

### 3. Sign-Out Flow

```
Client → POST /api/auth/signout Bearer {sessionId}
  → AccountService.signOut()
    → SessionRepository.findBySessionIdAndIsActiveTrue()
    → session.isActive = false
    → SessionRepository.save()
  ← 200 { message: "Sign out successful" }
  ← 401 if session invalid
```

### 4. Session Validation Flow

```
Client → POST /api/auth/validate Bearer {sessionId}
  → AccountService.validateSession()
    → SessionRepository.findBySessionIdAndIsActiveTrueAndExpiresAtAfter()
    → AccountRepository.findById()
    → ParentRepository.findById()
  ← 200 { accountId, loginId, parentId, parentName, isValid: true }
  ← 401 if session expired or invalid
```

---

## Database Schema

### Entity Relationship Diagram

```
┌───────────────────┐
│     accounts      │
├───────────────────┤
│ account_id   UUID │── PK
│ login_id     VARCHAR│── UNIQUE, NOT NULL
│ password_hash VARCHAR│── NOT NULL
│ created_at   BIGINT│
│ updated_at   BIGINT│
└────────┬──────────┘
         │ 1
         │
         ├──────────────────┐
         │ *                │ *
         ▼                  ▼
┌───────────────────┐  ┌───────────────────┐
│     parents       │  │     students      │
├───────────────────┤  ├───────────────────┤
│ parent_id   UUID  │──│ student_id  UUID  │
│ account_id  UUID  │  │ account_id  UUID  │
│ name        VARCHAR│  │ name        VARCHAR│
│ created_at  BIGINT│  │ gender      VARCHAR│
│ updated_at  BIGINT│  │ birth_year  INT   │
└────────┬──────────┘  │ class       VARCHAR│
         │ 1            │ created_at  BIGINT│
         │              │ updated_at  BIGINT│
         │ *            └───────────────────┘
         ▼
┌───────────────────┐
│     sessions      │
├───────────────────┤
│ session_id   UUID │── PK
│ account_id  UUID  │── FK → accounts
│ parent_id   UUID  │── FK → parents
│ is_active   BOOL  │── default true
│ created_at  BIGINT│
│ expires_at  BIGINT│── (now + 30 days)
└───────────────────┘
```

### SQL (H2 / PostgreSQL)

```sql
CREATE TABLE accounts (
  account_id UUID PRIMARY KEY,
  login_id VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at BIGINT,
  updated_at BIGINT
);
CREATE INDEX idx_accounts_login_id ON accounts(login_id);

CREATE TABLE parents (
  parent_id UUID PRIMARY KEY,
  account_id UUID NOT NULL,
  name VARCHAR(100),
  created_at BIGINT,
  updated_at BIGINT,
  FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);
CREATE INDEX idx_parents_account_id ON parents(account_id);

CREATE TABLE students (
  student_id UUID PRIMARY KEY,
  account_id UUID NOT NULL,
  name VARCHAR(100) NOT NULL,
  gender VARCHAR(10),
  birth_year INT,
  class VARCHAR(50),
  created_at BIGINT,
  updated_at BIGINT,
  FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);
CREATE INDEX idx_students_account_id ON students(account_id);

CREATE TABLE sessions (
  session_id UUID PRIMARY KEY,
  account_id UUID NOT NULL,
  parent_id UUID NOT NULL,
  is_active BOOLEAN DEFAULT true,
  created_at BIGINT,
  expires_at BIGINT NOT NULL,
  FOREIGN KEY (account_id) REFERENCES accounts(account_id),
  FOREIGN KEY (parent_id) REFERENCES parents(parent_id)
);
CREATE INDEX idx_sessions_session_id ON sessions(session_id);
CREATE INDEX idx_sessions_account_id ON sessions(account_id);
CREATE INDEX idx_sessions_parent_id ON sessions(parent_id);
CREATE INDEX idx_sessions_is_active ON sessions(is_active);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);
```

### Lifecycle Rules

| Entity | Event | Action |
|--------|-------|--------|
| `Account` | `@PrePersist` | Set `createdAt`, `updatedAt` to `now` |
| `Account` | `@PreUpdate` | Set `updatedAt` to `now` |
| `Parent` | `@PrePersist` | Set `createdAt`, `updatedAt` to `now` |
| `Parent` | `@PreUpdate` | Set `updatedAt` to `now` |
| `Student` | `@PrePersist` | Set `createdAt`, `updatedAt` to `now` |
| `Student` | `@PreUpdate` | Set `updatedAt` to `now` |
| `Session` | `@PrePersist` | Set `createdAt` to `now`, `isActive` to `true` |

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `INVALID_FORMAT` | 400 | LoginId format is invalid |
| `WEAK_PASSWORD` | 400 | Password < 6 characters |
| `MISSING_FIELDS` | 400 | Required fields not provided |
| `MISSING_SESSION_ID` | 400 | SessionId not in request |
| `BAD_HEADER` | 400 | Authorization header malformed |
| `INVALID_CREDENTIALS` | 401 | Wrong email/phone or password |
| `INVALID_SESSION` | 401 | Session expired or invalidated |
| `EMAIL_PHONE_EXISTS` | 409 | LoginId already registered |
| `PARENT_NOT_FOUND` | 404 | Parent profile not found |
| `STUDENT_NOT_FOUND` | 404 | Student profile not found |
| `UNAUTHORIZED_ACCESS` | 403 | Resource doesn't belong to this account |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

All error responses follow this structure:

```json
{
  "message": "Human-readable description",
  "errorCode": "ERROR_CODE",
  "timestamp": 1719907200000
}
```

---

## Configuration

### application.properties

```properties
server.port=8080
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
logging.level.com.studyshield.user=DEBUG
```

### Class Configuration

Edit `class-config.json` in resources to customize the class list. Structure:

```json
{
  "default": {
    "name": "Default",
    "classes": ["Nursery", "Junior KG", "Senior KG", "1st", ...]
  }
}
```

### CORS (WebConfiguration)

| Property | Value |
|----------|-------|
| Allowed Origins | `*` |
| Allowed Methods | `POST, GET, OPTIONS, PUT, DELETE` |
| Allowed Headers | `Content-Type, Authorization` |
| Max Age | 3600s |

### Security Headers

| Header | Value |
|--------|-------|
| `X-Content-Type-Options` | `nosniff` |
| `X-Frame-Options` | `DENY` |

---

## Running the Application

### Prerequisites

- Java 17+ (tested on Java 25)
- Gradle (wrapper included)

### Commands

```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Start application
./gradlew bootRun
```

The application starts at `http://localhost:8080`.
H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`)

### Quick Test

```bash
# Sign up
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"loginId":"test@example.com","password":"secure123","name":"Alice"}'

# Sign in
SESSION=$(curl -s -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"loginId":"test@example.com","password":"secure123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['sessionId'])")

# Validate session
curl -X POST http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer $SESSION"

# Add another parent
curl -X POST http://localhost:8080/api/parents \
  -H "Authorization: Bearer $SESSION" \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob"}'

# List parents
curl http://localhost:8080/api/parents \
  -H "Authorization: Bearer $SESSION"

# Add a student
curl -X POST http://localhost:8080/api/students \
  -H "Authorization: Bearer $SESSION" \
  -H "Content-Type: application/json" \
  -d '{"name":"Charlie","gender":"MALE","birthYear":2018,"studentClass":"1st"}'

# List students
curl http://localhost:8080/api/students \
  -H "Authorization: Bearer $SESSION"

# Sign out
curl -X POST http://localhost:8080/api/auth/signout \
  -H "Authorization: Bearer $SESSION"
```

### Swagger UI

When running, visit:
- `http://localhost:8080/swagger-ui.html` — Interactive API docs
- `http://localhost:8080/v3/api-docs` — OpenAPI JSON spec

---

## File Index

```
api/src/main/java/com/studyshield/user/
├── Account.java                      # Account entity (replaces User)
├── AccountRepository.java            # Account JPA repository
├── AccountService.java               # Auth + session business logic
├── AuthController.java               # Signup, signin, signout, validate
├── AuthResponse.java                 # Unified auth response DTO
├── ClassConfigController.java        # Static class config endpoint
├── GlobalExceptionHandler.java       # Centralized error handler
├── Parent.java                       # Parent entity
├── ParentController.java             # Parent CRUD endpoints
├── ParentRepository.java             # Parent JPA repository
├── ParentRequest.java                # Add/update parent DTO
├── ParentResponse.java               # Parent response DTO
├── ParentService.java                # Parent business logic
├── PasswordUtil.java                 # BCrypt utility
├── RegistrationException.java        # Custom exception
├── SecurityConfig.java               # Security configuration
├── Session.java                      # Session entity
├── SessionRepository.java            # Session JPA repository
├── SignInRequest.java                # Signin request DTO
├── SignOutRequest.java               # Signout request DTO
├── SignUpRequest.java                # Signup request DTO
├── Student.java                      # Student entity
├── StudentController.java            # Student CRUD endpoints
├── StudentRepository.java            # Student JPA repository
├── StudentRequest.java               # Add/update student DTO
├── StudentResponse.java              # Student response DTO
├── StudentService.java               # Student business logic
├── ValidationResponse.java           # Validation response DTO
└── WebConfiguration.java             # CORS + security headers

api/src/main/resources/
├── application.properties            # Spring Boot config
└── class-config.json                 # Static class list

docs/
├── README.md                         # This file
├── ARCHITECTURE.md                   # Architecture reference
├── signup-flow.puml                  # Sign-up sequence diagram
├── signin-flow.puml                  # Sign-in sequence diagram
├── signout-flow.puml                 # Sign-out sequence diagram
├── session-validation-flow.puml      # Session validation sequence diagram
└── database-schema.puml              # Database ER diagram
```
