# Study Shield User Service

A Spring Boot application with user registration API, MongoDB integration and Docker support.

## Features

- User registration with username and email (both optional)
- Secure unique user ID generation (better than UUID)
- Authentication token generation for mobile apps
- Full CRUD operations for users
- MongoDB Atlas support 
- Docker containerization

## API Endpoints

### User Registration
```
POST /api/users/register?username={username}&email={email}
```

Returns:
```json
{
  "userId": "USR_1234567890_1234",
  "authToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "message": "Registration successful"
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

The application uses MongoDB for data storage. By default, it connects to a local MongoDB instance. To use MongoDB Atlas, set the `MONGODB_URI` environment variable:

```
export MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/database"
```

## Security

- User IDs are generated using a combination of timestamp and random number for better security than UUID
- Authentication tokens are generated using UUID for mobile app session management