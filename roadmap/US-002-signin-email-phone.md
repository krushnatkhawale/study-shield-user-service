# US-002: User Sign-In with Email or Phone

## Story Metadata
- **ID:** US-002
- **Title:** POST /api/auth/signin - Email/Phone login
- **Priority:** P0 (Critical Path)
- **Story Points:** 3
- **Sprint:** MVP-1
- **Tags:** auth, signin, core

## User Story
**As a** mobile/web app user  
**I want to** sign in using email or phone + password  
**So that** I can access my account and get a session ID

## Business Context
- Enable returning users to re-authenticate
- Support both email and phone login routes
- Generate new session per login (multi-device support)

## Acceptance Criteria

### Functional Requirements
1. **Input Validation**
   - [ ] Accept `loginId` (email or phone format)
   - [ ] Accept `password` (required)
   - [ ] Return 400 if either field missing

2. **Business Logic**
   - [ ] Query User by loginId
   - [ ] Compare provided password with stored BCrypt hash
   - [ ] On match: generate new sessionId (UUID)
   - [ ] Create new Session record with 30-day expiry
   - [ ] Set session.isActive = true
   - [ ] Allow multiple active sessions per user (multi-device)

3. **Response Behavior**
   - [ ] Return HTTP 200 OK on success
   - [ ] Return HTTP 400 Bad Request if fields missing
   - [ ] Return HTTP 401 Unauthorized if invalid credentials (generic message for security)
   - [ ] Return HTTP 500 Internal Server Error on DB failure
   - [ ] **Security:** Never reveal if email/phone exists (always return 401)

### Non-Functional Requirements
- Response time: < 500ms (BCrypt verification is intentionally slow)
- Support concurrent logins
- CORS enabled for cross-origin requests

## Request/Response Specification

### Request
```json
POST /api/auth/signin
Content-Type: application/json

{
  "loginId": "user@example.com",  // or "+14155552671"
  "password": "SecurePass123"
}
```

### Success Response (200 OK)
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "loginId": "user@example.com",
  "sessionId": "770e8400-e29b-41d4-a716-446655440002",
  "message": "Sign in successful",
  "timestamp": 1719907200000
}
```

### Error Responses

**400 Bad Request - Missing Fields**
```json
{
  "message": "Email/phone and password required",
  "errorCode": "MISSING_FIELDS",
  "timestamp": 1719907200000
}
```

**401 Unauthorized - Invalid Credentials**
```json
{
  "message": "Invalid email/phone or password",
  "errorCode": "INVALID_CREDENTIALS",
  "timestamp": 1719907200000
}
```

**500 Internal Server Error**
```json
{
  "message": "Sign in failed",
  "errorCode": "INTERNAL_ERROR",
  "timestamp": 1719907200000
}
```

## Implementation Details

### Files to Create/Modify
- `SignInRequest.java` - DTO
- `SessionResponse.java` - Reuse from US-001
- `AuthController.java` - Add signin endpoint
- `AuthService.java` - Add signin business logic
- `UserRepository.java` - Add findByloginId method
- `SessionRepository.java` - Create session records

### UserRepository Query
```java
Optional<User> findByloginId(String loginId);
```

### Code Requirements
- Use @PostMapping("/signin")
- Use BCryptPasswordEncoder.matches(rawPassword, hash)
- Use @Transactional for DB operations
- Return generic error message (security best practice)
- Add CORS headers

### Testing Checklist
- [ ] Unit test: Valid signin with email
- [ ] Unit test: Valid signin with phone
- [ ] Unit test: Non-existent user
- [ ] Unit test: Wrong password
- [ ] Unit test: Case sensitivity check (email lowercase)
- [ ] Integration test: End-to-end signin flow
- [ ] Security test: Timing attack resistance (BCrypt is slow intentionally)

## Edge Cases
1. User tries to signin before completing email verification (MVP: no verification, so allowed)
2. Multiple failed signin attempts (MVP: no rate limiting, implement in phase 2)
3. User explicitly requests both email and phone in separated format - should fail (accept one format only)
4. Password contains leading/trailing spaces - should fail (require exact match)

## Dependencies
- `org.springframework.security:spring-security-crypto` (BCrypt)
- Database connection

## Definition of Done
- [ ] Code written per acceptance criteria
- [ ] All tests passing (>90% coverage)
- [ ] Security reviewed (no credential leaks in logs)
- [ ] Code reviewed and approved
- [ ] API tested with Postman/curl
- [ ] Merged to develop branch

