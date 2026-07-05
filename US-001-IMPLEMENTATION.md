# Study Shield User Service - US-001 Implementation

This project implements the user sign-up functionality for email or phone as specified in US-001.

## Implementation Overview

The implementation follows the exact requirements from the user story:
- Accepts email OR phone as loginId field (instead of separate fields)
- Validates password format 
- Hashes passwords using BCrypt
- Creates unique userId and sessionId
- Handles proper HTTP responses with status codes

## Key Files Created

1. **User.java** - Entity with loginId field instead of separate email/phone fields
2. **SignUpRequest.java** - DTO for signup input validation
3. **SessionResponse.java** - DTO for API response structure  
4. **Session.java** - Session tracking entity
5. **UserRepository.java** - JPA repository for user operations
6. **SessionRepository.java** - JPA repository for session operations
7. **PasswordUtil.java** - BCrypt password utility
8. **UserController.java** - REST endpoint with signup logic
9. **UserService.java** - Business logic implementation
10. **application.properties** - Spring Boot configuration

## API Endpoint

POST /api/auth/signup
Content-Type: application/json

{
  "loginId": "user@example.com",  
  "password": "SecurePass123"
}

Returns:
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "loginId": "user@example.com",
  "sessionId": "660e8400-e29b-41d4-a716-446655440001", 
  "message": "Sign up successful",
  "timestamp": 1719907200000
}

## To Compile and Run

```
cd /Users/hulk/IdeaProjects/study-shield-user-service
./gradlew clean build
./gradlew bootRun
```

The application will be available at http://localhost:8080

To test with curl:
```
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"loginId":"test@example.com","password":"secure123"}'
```