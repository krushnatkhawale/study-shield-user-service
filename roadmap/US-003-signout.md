# US-003: User Sign-Out

## Story Metadata
- **ID:** US-003
- **Title:** POST /api/auth/signout - Invalidate session
- **Priority:** P0
- **Story Points:** 2
- **Sprint:** MVP-1
- **Tags:** auth, signout, core

## User Story
**As a** mobile/web app user  
**I want to** sign out from my account  
**So that** my session token becomes invalid and I'm logged out

## Business Context
- Allow users to securely end their session
- Support multi-device sessions (invalidate one session without affecting others)
- Pass sessionId in Authorization header or request body

## Acceptance Criteria

### Functional Requirements
1. **Input Validation**
   - [ ] Accept `sessionId` from Authorization header (Bearer {sessionId}) OR from request body
   - [ ] Return 400 if sessionId missing

2. **Business Logic**
   - [ ] Find session by sessionId
   - [ ] Mark session.isActive = false (soft delete preferred over hard delete)
   - [ ] Preserve session history (for audit logs later)
   - [ ] Other sessions of same user remain active

3. **Response Behavior**
   - [ ] Return HTTP 200 OK on success
   - [ ] Return HTTP 401 Unauthorized if sessionId invalid/expired
   - [ ] Return HTTP 400 Bad Request if sessionId missing
   - [ ] Future requests with invalid sessionId return 401

### Non-Functional Requirements
- Response time: < 100ms (simple DB update)
- Support immediate session invalidation (no caching delay)

## Request/Response Specification

### Request Option 1: Authorization Header
```json
POST /api/auth/signout
Authorization: Bearer 660e8400-e29b-41d4-a716-446655440001
Content-Type: application/json
```

### Request Option 2: Request Body
```json
POST /api/auth/signout
Content-Type: application/json

{
  "sessionId": "660e8400-e29b-41d4-a716-446655440001"
}
```

### Success Response (200 OK)
```json
{
  "message": "Sign out successful",
  "timestamp": 1719907200000
}
```

### Error Responses

**400 Bad Request - Missing sessionId**
```json
{
  "message": "Session ID required",
  "errorCode": "MISSING_SESSION_ID",
  "timestamp": 1719907200000
}
```

**401 Unauthorized - Invalid sessionId**
```json
{
  "message": "Invalid or expired session",
  "errorCode": "INVALID_SESSION",
  "timestamp": 1719907200000
}
```

## Implementation Details

### Files to Create/Modify
- `SignOutRequest.java` - DTO (optional, if using body)
- `AuthController.java` - Add signout endpoint
- `AuthService.java` - Add signout logic
- `SessionRepository.java` - Add invalidate method

### SessionRepository Method
```java
void invalidateSession(String sessionId);
// Or: Session findBySessionIdAndIsActiveTrue(String sessionId);
```

### Code Requirements
- Add @PostMapping("/signout")
- Extract sessionId from header (Bearer token) or body
- Use @Transactional
- Soft delete: UPDATE sessions SET is_active = FALSE WHERE session_id = ?
- Return simple message response
- Add CORS headers

### Testing Checklist
- [ ] Unit test: Valid signout
- [ ] Unit test: Invalid sessionId
- [ ] Unit test: Already invalid session
- [ ] Unit test: sessionId from header
- [ ] Unit test: sessionId from body
- [ ] Integration test: End-to-end signout flow
- [ ] Multi-device test: Signout one device, other sessions still active

## Edge Cases
1. User tries to signout twice with same sessionId - Should succeed both times (idempotent is OK for MVP, or return 401 second time)
2. sessionId from header takes precedence over body - Document behavior
3. Concurrent signout requests same sessionId - Use DB row-level locking

## Dependencies
- Database with transactional support

## Definition of Done
- [ ] Code written per acceptance criteria
- [ ] All tests passing
- [ ] Code reviewed
- [ ] API tested with Postman
- [ ] Merged to develop branch

