# US-001: User Sign-Up with Email or Phone

## Story Metadata
- **ID:** US-001
- **Title:** POST /api/auth/signup - Email/Phone registration
- **Priority:** P0 (Critical Path)
- **Story Points:** 3
- **Sprint:** MVP-1
- **Tags:** auth, signup, core

## User Story
**As a** mobile/web app user  
**I want to** sign up using email or phone + password  
**So that** I can create an account and receive a session ID for authenticated requests

## Business Context
- Support multi-platform clients (Android, iOS, Web)
- Simplify authentication with single loginId field
- Enable rapid MVP deployment

## Acceptance Criteria

### Functional Requirements
1. **Input Validation**
   - [ ] Accept email of phone as `loginId` (email OR international phone format: +[country_code][number])
   - [ ] Accept `password` (min 6 characters)
   - [ ] Validate email format using standard regex
   - [ ] Validate phone format (international: +1-15 digits)
   - [ ] Reject if either field missing

2. **Business Logic**
   - [ ] Check if loginId already exists in User table
   - [ ] Hash password using BCrypt (salt rounds: 10)
   - [ ] Generate unique userId (UUID)
   - [ ] Generate unique sessionId (UUID)
   - [ ] Create User record in DB
   - [ ] Create Session record with 30-day expiry
   - [ ] Set session.isActive = true

3. **Response Behavior**
   - [ ] Return HTTP 201 Created on success
   - [ ] Return HTTP 400 Bad Request on validation failure
   - [ ] Return HTTP 409 Conflict if loginId exists
   - [ ] Return HTTP 500 Internal Server Error on DB failure

### Non-Functional Requirements
- Response time: < 500ms (DB + BCrypt hashing)
- Support concurrent signups (DB transactions)
- CORS headers for cross-origin requests

## Request/Response Specification

### Request
```json
POST /api/auth/signup
Content-Type: application/json

{
  "loginId": "user@example.com",  // or "+14155552671"
  "password": "SecurePass123"
}
```

### Success Response (201 Created)
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "loginId": "user@example.com",
  "sessionId": "660e8400-e29b-41d4-a716-446655440001",
  "message": "Sign up successful",
  "timestamp": 1719907200000
}
```

### Error Responses

**400 Bad Request - Validation Error**
```json
{
  "message": "Enter valid email or phone (+[country_code][number])",
  "errorCode": "INVALID_FORMAT",
  "timestamp": 1719907200000
}
```

**400 Bad Request - Weak Password**
```json
{
  "message": "Password must be at least 6 characters",
  "errorCode": "WEAK_PASSWORD",
  "timestamp": 1719907200000
}
```

**409 Conflict - Already Registered**
```json
{
  "message": "Email or phone already registered",
  "errorCode": "EMAIL_PHONE_EXISTS",
  "timestamp": 1719907200000
}
```

**500 Internal Server Error**
```json
{
  "message": "Sign up failed",
  "errorCode": "INTERNAL_ERROR",
  "timestamp": 1719907200000
}
```

## Implementation Details

### Files to Create/Modify
- `SignUpRequest.java` - DTO with validation
- `SessionResponse.java` - Response DTO
- `AuthController.java` - Endpoint handler
- `AuthService.java` - Business logic
- `User.java` - Entity with loginId field
- `Session.java` - Entity for session tracking
- `SessionRepository.java` - JPA repository

### Database Table: users
```sql
CREATE TABLE users (
  id UUID PRIMARY KEY,
  loginId VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Database Table: sessions
```sql
CREATE TABLE sessions (
  session_id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Code Requirements
- Use Spring Boot @PostMapping annotation
- Use @Valid for request validation
- Use BCryptPasswordEncoder bean (Spring Security)
- Use @Transactional for DB operations
- Return ResponseEntity<SessionResponse>
- Add CORS headers in response

### Testing Checklist
- [ ] Unit test: Valid email signup
- [ ] Unit test: Valid phone signup
- [ ] Unit test: Invalid email format
- [ ] Unit test: Invalid phone format
- [ ] Unit test: Weak password
- [ ] Unit test: Duplicate email/phone
- [ ] Integration test: End-to-end signup flow
- [ ] Load test: Concurrent signups (100 users)

## Edge Cases
1. Special characters in password (e.g., @, #, $, %) - Should be allowed
2. Phone number with spaces (e.g., "+1 415 555 2671") - Should be rejected/trimmed
3. Email with subdomain (e.g., "user+tag@example.co.uk") - Should be allowed
4. Multiple signups from same IP in 1 second - Should succeed (no rate limiting in MVP)

## Dependencies
- `org.springframework.security:spring-security-crypto` (BCrypt)
- `org.springframework.boot:spring-boot-starter-validation`
- Database: H2 (dev) / PostgreSQL (prod)

## Definition of Done
- [ ] Code written per acceptance criteria
- [ ] All tests passing (100% coverage for service layer)
- [ ] Code review approved
- [ ] API documentation updated (Swagger/OpenAPI)
- [ ] Manual testing on Postman/Insomnia
- [ ] No security vulnerabilities (OWASP)
- [ ] Merged to develop branch

