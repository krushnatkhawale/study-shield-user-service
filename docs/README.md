# Study Shield User Service — Documentation

> A Spring Boot REST API for user authentication supporting email/phone registration,
> sign-in, sign-out, and session validation.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [API Endpoints](#api-endpoints)
3. [Flow Diagrams](#flow-diagrams)
   - [Sign-Up Flow (US-001)](#1-sign-up-flow-us-001)
   - [Sign-In Flow (US-002)](#2-sign-in-flow-us-002)
   - [Sign-Out Flow (US-003)](#3-sign-out-flow-us-003)
   - [Session Validation Flow (US-004)](#4-session-validation-flow-us-004)
4. [Database Schema](#database-schema)
5. [Error Codes](#error-codes)
6. [Configuration](#configuration)
7. [Running the Application](#running-the-application)

---

## Architecture Overview

The application follows a layered Spring Boot architecture:

```
┌─────────────────────────────────────────────────────┐
│                   Client Layer                       │
│   Mobile App (Android/iOS)    Web App (React)        │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP REST (JSON)
                       ▼
┌─────────────────────────────────────────────────────┐
│               API Gateway (Port 8080)                │
│   ┌──────────────┐  ┌──────────────────────────┐    │
│   │ CORS Filter   │  │  Security Filter Chain   │    │
│   └──────────────┘  └──────────────────────────┘    │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│               Business Logic Layer                   │
│   ┌──────────────┐  ┌──────────┐  ┌─────────────┐  │
│   │UserController│→ │UserService│→ │PasswordUtil │  │
│   └──────────────┘  └─────┬────┘  └─────────────┘  │
│                           │          (BCrypt)       │
│   ┌───────────────────┐  │                          │
│   │GlobalExceptionHdlr│  │                          │
│   └───────────────────┘  │                          │
└──────────────────────────┼──────────────────────────┘
                           │ JPA / Hibernate
                           ▼
┌─────────────────────────────────────────────────────┐
│                  Data Layer                          │
│   ┌─────────────┐  ┌─────────────┐                  │
│   │   users     │  │  sessions   │                  │
│   └─────────────┘  └─────────────┘                  │
│              H2 (dev) / PostgreSQL (prod)            │
└─────────────────────────────────────────────────────┘
```

### Key Components

| Component | Package | Responsibility |
|-----------|---------|---------------|
| `UserController` | `com.studyshield.user` | REST endpoint handlers |
| `UserService` | `com.studyshield.user` | Business logic — signup, auth, session mgmt |
| `PasswordUtil` | `com.studyshield.user` | BCrypt encoding & verification |
| `GlobalExceptionHandler` | `com.studyshield.user` | Centralized error handling (`@ControllerAdvice`) |
| `WebConfiguration` | `com.studyshield.user` | CORS + security headers |
| `SecurityConfig` | `com.studyshield.user` | Spring Security (CSRF off, permit `/api/auth/**`) |

---

## API Endpoints

### `POST /api/auth/signup` — User Registration (US-001)

**Request:**
```json
{
  "loginId": "user@example.com",
  "password": "SecurePass123"
}
```

**Success (201):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "loginId": "user@example.com",
  "sessionId": "660e8400-e29b-41d4-a716-446655440001",
  "message": "Sign up successful",
  "errorCode": null,
  "timestamp": 1719907200000
}
```

**Errors:** `400` (invalid format), `409` (duplicate), `500` (internal)

---

### `POST /api/auth/signin` — User Authentication (US-002)

**Request:**
```json
{
  "loginId": "user@example.com",
  "password": "SecurePass123"
}
```

**Success (200):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "loginId": "user@example.com",
  "sessionId": "770e8400-e29b-41d4-a716-446655440002",
  "message": "Sign in successful",
  "errorCode": null,
  "timestamp": 1719907200000
}
```

**Errors:** `400` (missing fields), `401` (invalid credentials — generic message)

---

### `POST /api/auth/signout` — Session Invalidation (US-003)

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
  "userId": null,
  "loginId": null,
  "sessionId": null,
  "message": "Sign out successful",
  "errorCode": null,
  "timestamp": 1719907200000
}
```

**Errors:** `400` (missing sessionId), `401` (invalid/expired session)

---

### `POST /api/auth/validate` — Session Validation (US-004)

**Request:**
```
Authorization: Bearer 660e8400-e29b-41d4-a716-446655440001
```

**Success (200):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "loginId": "user@example.com",
  "isValid": true,
  "message": "Session valid",
  "errorCode": null,
  "timestamp": 1719907200000
}
```

**Errors:** `400` (malformed header), `401` (expired/invalid session)

---

## Flow Diagrams

PlantUML source files are available in the [`docs/`](./) directory.

### 1. Sign-Up Flow (US-001)

```
┌──────┐     ┌────────────────┐     ┌────────────┐     ┌─────────────┐     ┌──────────┐
│Client│     │UserController  │     │UserService │     │PasswordUtil │     │Database  │
└──┬───┘     └───────┬────────┘     └──────┬─────┘     └──────┬──────┘     └────┬─────┘
   │                 │                      │                  │                 │
   │  POST /signup   │                      │                  │                 │
   │  {loginId,pwd}  │                      │                  │                 │
   │────────────────>│                      │                  │                 │
   │                 │  validate fields     │                  │                 │
   │                 │─────────────────────▶│                  │                 │
   │                 │                      │  findByLoginId   │                 │
   │                 │                      │─────────────────▶│                 │
   │                 │                      │◀─────────────────│                 │
   │                 │                      │   exists?        │                 │
   │                 │   409 if exists      │                  │                 │
   │◀────────────────│                      │                  │                 │
   │                 │                      │  encodePassword  │                 │
   │                 │                      │─────────────────▶│                 │
   │                 │                      │◀─────────────────│                 │
   │                 │                      │  BCrypt hash     │                 │
   │                 │                      │                  │                 │
   │                 │                      │  save(User)      │                 │
   │                 │                      │─────────────────▶│                 │
   │                 │                      │◀─────────────────│                 │
   │                 │                      │  savedUser       │                 │
   │                 │                      │                  │                 │
   │                 │                      │  save(Session)   │                 │
   │                 │                      │─────────────────▶│                 │
   │                 │                      │◀─────────────────│                 │
   │                 │  SessionResponse     │                  │                 │
   │                 │◀─────────────────────│                  │                 │
   │  201 Created    │                      │                  │                 │
   │◀────────────────│                      │                  │                 │
```

### 2. Sign-In Flow (US-002)

```
┌──────┐     ┌────────────────┐     ┌────────────┐     ┌─────────────┐     ┌──────────┐
│Client│     │UserController  │     │UserService │     │PasswordUtil │     │Database  │
└──┬───┘     └───────┬────────┘     └──────┬─────┘     └──────┬──────┘     └────┬─────┘
   │                 │                      │                  │                 │
   │  POST /signin   │                      │                  │                 │
   │  {loginId,pwd}  │                      │                  │                 │
   │────────────────>│                      │                  │                 │
   │                 │  validate fields     │                  │                 │
   │                 │─────────────────────▶│ authenticate()   │                 │
   │                 │                      │                  │                 │
   │                 │                      │  findByLoginId   │                 │
   │                 │                      │─────────────────▶│                 │
   │                 │                      │◀─────────────────│                 │
   │                 │                      │                  │                 │
   │                 │                      │  matches(pwd)    │                 │
   │                 │                      │─────────────────▶│                 │
   │                 │                      │◀─────────────────│                 │
   │                 │                      │  true/false      │                 │
   │                 │                      │                  │                 │
   │                 │   401 if invalid     │                  │                 │
   │◀────────────────│                      │                  │                 │
   │                 │                      │  save(Session)   │                 │
   │                 │                      │─────────────────▶│                 │
   │                 │                      │◀─────────────────│                 │
   │                 │  SessionResponse     │                  │                 │
   │                 │◀─────────────────────│                  │                 │
   │  200 OK         │                      │                  │                 │
   │◀────────────────│                      │                  │                 │
```

### 3. Sign-Out Flow (US-003)

```
┌──────┐     ┌────────────────┐     ┌────────────┐     ┌──────────┐
│Client│     │UserController  │     │UserService │     │Database  │
└──┬───┘     └───────┬────────┘     └──────┬─────┘     └────┬─────┘
   │                 │                      │                 │
   │  POST /signout  │                      │                 │
   │  Bearer {sid}   │                      │                 │
   │  or {sessionId} │                      │                 │
   │────────────────>│                      │                 │
   │                 │  extract sessionId   │                 │
   │                 │─────────────────────▶│ signOut(sid)    │
   │                 │                      │                 │
   │                 │                      │ findSession()   │
   │                 │                      │───────────────▶│
   │                 │                      │◀───────────────│
   │                 │                      │                 │
   │                 │  401 if invalid      │                 │
   │◀────────────────│                      │                 │
   │                 │                      │ setActive=false │
   │                 │                      │───────────────▶│
   │                 │                      │◀───────────────│
   │                 │  void (success)      │                 │
   │                 │◀─────────────────────│                 │
   │  200 OK         │                      │                 │
   │◀────────────────│                      │                 │
```

### 4. Session Validation Flow (US-004)

```
┌──────┐     ┌────────────────┐     ┌────────────┐     ┌──────────┐
│Client│     │UserController  │     │UserService │     │Database  │
└──┬───┘     └───────┬────────┘     └──────┬─────┘     └────┬─────┘
   │                 │                      │                 │
   │  POST /validate │                      │                 │
   │  Bearer {sid}   │                      │                 │
   │────────────────>│                      │                 │
   │                 │  parse header        │                 │
   │                 │─────────────────────▶│ validate(sid)   │
   │                 │                      │                 │
   │                 │                      │ findBySessionId │
   │                 │                      │ &IsActiveTrue   │
   │                 │                      │ &ExpiresAtAfter │
   │                 │                      │───────────────▶│
   │                 │                      │◀───────────────│
   │                 │                      │                 │
   │                 │                      │ findUserById()  │
   │                 │                      │───────────────▶│
   │                 │                      │◀───────────────│
   │                 │                      │                 │
   │                 │  401 if invalid      │                 │
   │◀────────────────│                      │                 │
   │                 │  ValidationResponse  │                 │
   │                 │◀─────────────────────│                 │
   │  200 OK         │                      │                 │
   │  {isValid=true} │                      │                 │
   │◀────────────────│                      │                 │
```

---

## Database Schema

### Entity Relationship Diagram

```
┌─────────────────────────────┐
│          users              │
├─────────────────────────────┤
│ id              UUID  ██←──│── PK, INDEX
│ login_id        VARCHAR(255)│── UNIQUE, NOT NULL, INDEX
│ password_hash   VARCHAR(255)│── NOT NULL
│ created_at      BIGINT      │── auto (PrePersist)
│ updated_at      BIGINT      │── auto (PreUpdate)
└──────────┬──────────────────┘
           │ 1
           │
           │ * (FK: user_id → id)
           │
┌──────────┴──────────────────┐
│         sessions            │
├─────────────────────────────┤
│ session_id       UUID  ██←──│── PK, INDEX
│ user_id          UUID       │── FK → users.id, INDEX
│ is_active        BOOLEAN    │── default true, INDEX
│ created_at       BIGINT     │── auto (PrePersist)
│ expires_at       BIGINT     │── NOT NULL (now + 30d), INDEX
└─────────────────────────────┘
```

### SQL (H2 / PostgreSQL)

```sql
CREATE TABLE users (
  id UUID PRIMARY KEY,
  login_id VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at BIGINT,
  updated_at BIGINT
);

CREATE INDEX idx_users_login_id ON users(login_id);

CREATE TABLE sessions (
  session_id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  is_active BOOLEAN DEFAULT true,
  created_at BIGINT,
  expires_at BIGINT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_sessions_session_id ON sessions(session_id);
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_is_active ON sessions(is_active);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);
```

### Lifecycle Rules

| Entity | Event | Action |
|--------|-------|--------|
| `User` | `@PrePersist` | Set `createdAt`, `updatedAt` to `now` |
| `User` | `@PreUpdate` | Set `updatedAt` to `now` |
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
| `INTERNAL_ERROR` | 500 | Unexpected server error |

All error responses follow this structure:

```json
{
  "userId": null,
  "loginId": null,
  "sessionId": null,
  "message": "Human-readable description",
  "errorCode": "ERROR_CODE",
  "timestamp": 1719907200000
}
```

---

## Configuration

### application.properties

```properties
# Server
server.port=8080

# H2 Database (dev)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Logging
logging.level.com.studyshield.user=DEBUG
```

### CORS (WebConfiguration)

| Property | Value |
|----------|-------|
| Allowed Origins | `*` (dev) |
| Allowed Methods | `POST, GET, OPTIONS, PUT, DELETE` |
| Allowed Headers | `Content-Type, Authorization` |
| Exposed Headers | `Authorization, Content-Type` |
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

# Start on custom port
./gradlew bootRun --args='--server.port=9090'
```

The application starts at `http://localhost:8080`.
H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`)

### Quick Test

```bash
# Sign up
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"loginId":"test@example.com","password":"secure123"}'

# Sign in
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"loginId":"test@example.com","password":"secure123"}'

# Validate session (replace with actual sessionId)
curl -X POST http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer YOUR_SESSION_ID"

# Sign out (replace with actual sessionId)
curl -X POST http://localhost:8080/api/auth/signout \
  -H "Authorization: Bearer YOUR_SESSION_ID"
```

---

## File Index

```
api/src/main/java/com/studyshield/
├── UserServiceApplication.java          # Spring Boot entry point
└── user/
    ├── User.java                         # User entity
    ├── Session.java                      # Session entity
    ├── UserController.java               # REST controller
    ├── UserService.java                  # Business logic
    ├── UserRepository.java               # User JPA repository
    ├── SessionRepository.java            # Session JPA repository
    ├── PasswordUtil.java                 # BCrypt utility
    ├── RegistrationException.java        # Custom exception
    ├── SecurityConfig.java               # Security config
    ├── WebConfiguration.java             # CORS + headers
    ├── GlobalExceptionHandler.java       # Centralized error handler
    ├── SignUpRequest.java                # Signup DTO
    ├── SignInRequest.java                # Signin DTO
    ├── SignOutRequest.java               # Signout DTO (body)
    ├── SessionResponse.java              # Response DTO
    └── ValidationResponse.java           # Validation DTO

docs/
├── README.md                             # This file
├── architecture.puml                     # System architecture diagram
├── signup-flow.puml                      # Sign-up sequence diagram
├── signin-flow.puml                      # Sign-in sequence diagram
├── signout-flow.puml                     # Sign-out sequence diagram
├── session-validation-flow.puml          # Session validation sequence diagram
└── database-schema.puml                  # Database ER diagram
```
