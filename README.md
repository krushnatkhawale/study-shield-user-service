# Study Shield User Service

A Spring Boot application with user registration API, MongoDB integration and Docker support.

## Features

- User registration with email or phone (both optional)
- Secure unique user ID generation (better than UUID)
- Authentication token generation for mobile apps
- Full CRUD operations for users
- MongoDB Atlas support 
- Docker containerization

## API Endpoints

### User Registration
```
POST /api/auth/signup
Content-Type: application/json

{
  "loginId": "user@example.com",  // or "+14155552671"
  "password": "SecurePass123"
}
```

Returns:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "loginId": "user@example.com",
  "sessionId": "660e8400-e29b-41d4-a716-446655440001",
  "message": "Sign up successful",
  "timestamp": 1719907200000
}
```

### Get User by ID
```
GET /api/users/{userId}
```

### Update User
```
PUT /api/users/{userId}?username={username}&email={email}
```

### Delete User
```
DELETE /api/users/{userId}
```

### Get All Users
```
GET /api/users
```

## Getting Started

### Prerequisites
- Java 17
- Docker (for containerization)

### Running Locally
1. Start MongoDB locally or configure connection in `application.yml`
2. Build the project: `./gradlew build`
3. Run the application: `./gradlew bootRun`

### Building Docker Image
```
docker build -t study-shield-user-service .
```

## Configuration

The application uses H2 in-memory database for development. For production, configure MongoDB:

```
export MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/database"
```

## Security

- User IDs are generated using a combination of timestamp and random number for better security than UUID
- Authentication tokens are generated using UUID for mobile app session management
- Passwords are hashed with BCrypt (10 salt rounds)

## Implementation Details

This implementation satisfies US-001: User Sign-Up with Email or Phone.

Key features implemented:
- LoginId accepts email or phone numbers 
- Password is validated with minimum 6 character requirement
- BCrypt password hashing
- Session management
- Proper error handling (400, 409, 500 responses)
- Database schema with users and sessions tables

## Testing

To build the application:
```
./gradlew build
```

To run tests:
```
./gradlew test
```

To run the application:
```
./gradlew bootRun
```

The API is accessible at http://localhost:8080
H2 Console for database inspection: http://localhost:8080/h2-console