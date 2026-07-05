# User Story 5: HTTP Headers & CORS Configuration

## Story Metadata
- **ID:** US-005
- **Title:** Configure HTTP Headers & CORS for multi-platform clients  
- **Priority:** P1 (Blocker for mobile/web clients)
- **Story Points:** 2
- **Sprint:** MVP-1
- **Tags:** infra, cors, security

## User Story
**As a** mobile/web client developer  
**I want to** make auth requests from any origin without CORS errors  
**So that** Android, iOS, and web apps can all call the API

## Business Context
- Support cross-origin requests (web apps, mobile webviews)
- Standardize response structure across all endpoints
- Enable secure communication with proper headers

## Acceptance Criteria

### Functional Requirements
1. **CORS Headers (all endpoints)**
   - [x] Add `Access-Control-Allow-Origin` header (dev: *, prod: specific domains)
   - [x] Add `Access-Control-Allow-Methods: POST, GET, OPTIONS`
   - [x] Add `Access-Control-Allow-Headers: Content-Type, Authorization`
   - [x] Allow OPTIONS pre-flight requests (respond with 200)

2. **Standard Response Structure (all endpoints)**
   - [x] All responses include `message` field (success or error)
   - [x] All responses include `timestamp` (ISO 8601)
   - [x] Error responses include `errorCode` field (enum)
   - [x] JSON format: consistent camelCase

3. **HTTP Status Codes**
   - [x] 201 Created → Successful signup
   - [x] 200 OK → Successful signin, signout, validate
   - [x] 400 Bad Request → Validation errors, missing fields
   - [x] 401 Unauthorized → Invalid credentials, expired session
   - [x] 409 Conflict → Resource exists (email/phone duplicate)
   - [x] 500 Internal Server Error → Unexpected errors

4. **Security Headers**
   - [x] Add `X-Content-Type-Options: nosniff`
   - [x] Add `X-Frame-Options: DENY` (prevent clickjacking)
   - [x] Add `Content-Type: application/json` explicitly
   - [x] Avoid exposing stack traces in production errors

### Non-Functional Requirements
- CORS pre-flight response < 50ms
- Headers consistent across all responses
- Security-focused (no information leaks)

## Request/Response Specification

### CORS Headers (all responses)
```
HTTP/1.1 200 OK
Content-Type: application/json
Access-Control-Allow-Origin: * (or https://yourdomain.com)
Access-Control-Allow-Methods: POST, GET, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
```

### Standard Success Response Body (all endpoints)
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

### Standard Error Response Body (all endpoints)
```json
{
  "userId": null,
  "loginId": null,
  "sessionId": null,
  "message": "Invalid email or phone format",
  "errorCode": "INVALID_FORMAT",
  "timestamp": 1719907200000
}
```

## Implementation Details

### Files Created/Modified
- `WebConfiguration.java` - NEW, Spring WebMvcConfigurer for CORS
- `GlobalExceptionHandler.java` - NEW, @ControllerAdvice for error responses  
- `ResponseWrapper.java` - NEW, generic response DTO
- `UserController.java` - Refactored to use ResponseWrapper
- `application-dev.properties` - Added CORS configuration

### Code Requirements
- Implement WebMvcConfigurer interface
- Use global CORS configuration (preferred over @CrossOrigin)
- Create GlobalExceptionHandler with @ControllerAdvice
- Wrap all responses with ResponseWrapper
- Add security headers via HttpServletResponse
- Return consistent error structure across all endpoints

### Testing Checklist
- [x] Compile successfully without errors  
- [x] CORS configuration implemented and tested
- [x] Response wrapper structure (all fields)
- [x] Error responses structure consistent
- [x] All existing functionality maintained
- [ ] Manual testing with web and mobile clients (pending)

## Configuration

### Environment-Specific CORS
**Development (application-dev.properties):**
```properties
cors.allowed-origins=*
```

**Production (application-prod.properties):**
```properties
cors.allowed-origins=https://yourapp.com,https://api.yourapp.com
```

## Error Codes Reference
```
INVALID_FORMAT - Email/phone format invalid
WEAK_PASSWORD - Password strength insufficient  
MISSING_FIELDS - Required fields missing
EMAIL_PHONE_EXISTS - Email/phone already registered
INVALID_CREDENTIALS - Wrong email/phone or password
INVALID_SESSION - Session invalid or expired
VALIDATION_ERROR - Generic validation error
INTERNAL_ERROR - Unexpected server error
```

## Dependencies
- `spring-boot-starter-web` (includes CORS support)
- `org.springdoc:springdoc-openapi-starter-webmvc-ui` (optional, for API docs)

## Definition of Done
- [x] CORS configuration implemented and tested
- [x] GlobalExceptionHandler catches all exceptions  
- [x] ResponseWrapper used across all endpoints
- [x] Error codes standardized
- [x] All build tests passing
- [x] Code reviewed
- [x] Merged to develop branch

## Note on Implementation  
The implementation includes:
1. Global CORS configuration allowing requests from any origin in development
2. Standardized response format using ResponseWrapper for all endpoints
3. Consistent error handling with GlobalExceptionHandler
4. Proper HTTP status codes and standardized error responses
5. Security headers for the API endpoints

This addresses all requirements of US-005 and allows mobile, web, and other clients to make cross-origin requests without CORS errors.