# US-004: Session Validation & Protected Routes Setup

## Story Metadata
- **ID:** US-004
- **Title:** POST /api/auth/validate - Validate active session
- **Priority:** P1 (Critical for protected endpoints)
- **Story Points:** 3
- **Sprint:** MVP-1
- **Tags:** auth, middleware, validation

## User Story
**As a** mobile/web app client  
**I want to** validate if my session is still active  
**So that** I can redirect to login if expired

## Business Context
- Create reusable session validation logic (middleware/interceptor)
- Provide health check endpoint for clients
- Establish foundation for protected API endpoints
- Support Authorization header parsing

## Acceptance Criteria

### Functional Requirements
1. **Input Validation**
   - [ ] Accept `sessionId` from Authorization header (Bearer {sessionId})
   - [ ] Return 401 if sessionId missing or malformed

2. **Business Logic**
   - [ ] Query Session by sessionId
   - [ ] Check session.isActive = true
   - [ ] Check session.expiresAt > NOW()
   - [ ] Return user details if valid
   - [ ] Invalidate or ignore if session expired

3. **Response Behavior**
   - [ ] Return HTTP 200 OK if session valid
   - [ ] Return HTTP 401 Unauthorized if invalid/expired
   - [ ] Return HTTP 400 Bad Request if header malformed

### Non-Functional Requirements
- Response time: < 100ms (simple query)
- Can be called frequently (on each API request)
- Reusable as middleware/filter for protected endpoints

## Request/Response Specification

### Request
```json
POST /api/auth/validate
Authorization: Bearer 660e8400-e29b-41d4-a716-446655440001
Content-Type: application/json
```

### Success Response (200 OK)
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "loginId": "user@example.com",
  "isValid": true,
  "message": "Session valid",
  "timestamp": 1719907200000
}
```

### Error Responses

**400 Bad Request - Malformed Header**
```json
{
  "message": "Invalid Authorization header format",
  "errorCode": "BAD_HEADER",
  "timestamp": 1719907200000
}
```

**401 Unauthorized - Invalid/Expired Session**
```json
{
  "message": "Session expired or invalid",
  "errorCode": "INVALID_SESSION",
  "timestamp": 1719907200000
}
```

## Implementation Details

### Files to Create/Modify
- `ValidationResponse.java` - Response DTO
- `AuthController.java` - Add validate endpoint
- `AuthService.java` - Add validate logic
- `AuthenticationFilter.java` - NEW, middleware for protected endpoints
- `SessionRepository.java` - Add query methods

### SessionRepository Methods
```java
Optional<Session> findBySessionIdAndIsActiveTrueAndExpiresAtAfter(String sessionId, LocalDateTime now);
```

### Code Requirements
- Add @PostMapping("/validate")
- Extract Authorization header: `Authorization: Bearer {sessionId}`
- Parse bearer token: `header.substring("Bearer ".length())`
- Use @Transactional(readOnly = true) - readonly query
- Return user details + isValid flag
- Build reusable filter/interceptor:
  - Implement `OncePerRequestFilter` or `HandlerInterceptor`
  - Apply to protected endpoints (all except /auth/signup, /auth/signin, etc.)
  - Inject sessionId to request context for downstream handlers

### Testing Checklist
- [ ] Unit test: Valid, active session
- [ ] Unit test: Invalid sessionId
- [ ] Unit test: Expired session (check expiresAt)
- [ ] Unit test: Already invalid session (is_active = false)
- [ ] Unit test: Malformed header (missing Bearer, typo)
- [ ] Unit test: Missing Authorization header
- [ ] Integration test: Middleware rejects invalid session for protected endpoint
- [ ] Integration test: Middleware allows valid session for protected endpoint

## Edge Cases
1. Session expires exactly at NOW() - Should be rejected (use <= for expiry check)
2. Multiple validation calls in pipeline - Should work idempotently
3. Bearer token with extra spaces - Trim before parsing
4. Case sensitivity of "Bearer" - Should be case-insensitive or enforce "Bearer" exactly

## Protected Endpoints (Future)
These endpoints will use the validation middleware:
- GET /api/user/profile
- PUT /api/user/profile
- DELETE /api/user/account
- Any future authenticated endpoint

## Dependencies
- Spring Web (filters/interceptors)
- Database session table

## Definition of Done
- [ ] Code written per acceptance criteria
- [ ] All tests passing (>90% coverage)
- [ ] Middleware tested with protected endpoints
- [ ] Code reviewed
- [ ] API tested with Postman
- [ ] Merged to develop branch

