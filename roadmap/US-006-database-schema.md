# US-006: Database Schema Updates - User & Session Tables

## Story Metadata
- **ID:** US-006
- **Title:** Create/Update database schema for auth system
- **Priority:** P0 (Blocking all other stories)
- **Story Points:** 2
- **Sprint:** MVP-1
- **Tags:** database, schema, jpa

## Story
**As a** backend developer  
**I want to** have properly structured User and Session tables in the database  
**So that** all authentication features can store and retrieve data reliably

## Business Context
- MVP uses `loginId` as single field for simplicity
- Support BCrypt password hashing (255+ char field)
- Track sessions for stateful authentication
- Enable audit logging (timestamps)

## Acceptance Criteria

### Database Schema Changes

#### 1. Update `users` Table
- [ ] Replace separate email/phone with `loginId` (UNIQUE, VARCHAR 255)
- [ ] Add `password_hash` (VARCHAR 255, BCrypt)
- [ ] Keep existing `user_id` (UUID)
- [ ] Add `created_at` (TIMESTAMP, auto)
- [ ] Add `updated_at` (TIMESTAMP, auto)
- [ ] Remove old email/password fields if they exist
- [ ] Add indexes on `loginId`

#### 2. Create `sessions` Table (NEW)
- [ ] `session_id` (UUID, PK)
- [ ] `user_id` (UUID, FK → users.user_id)
- [ ] `is_active` (BOOLEAN, default true)
- [ ] `created_at` (TIMESTAMP, auto)
- [ ] `expires_at` (TIMESTAMP, auto, +30 days)
- [ ] Add indexes on `session_id`, `user_id`, `is_active`
- [ ] Add FOREIGN KEY constraint to users table

#### 3. Data Migration (if existing data)
- [ ] Back up existing users table
- [ ] Migrate email column → loginId (keep data)
- [ ] Hash existing passwords with BCrypt (if any) or reset
- [ ] Delete old phone column if exists
- [ ] Verify data integrity

### JPA Entity Changes

#### User.java (Existing Entity)
```java
@Entity
@Table(name = "users")
public class User {
  @Id
  private String id;  // UUID, already exists
  
  @Column(unique = true, nullable = false, length = 255)
  private String loginId;  // NEW FIELD: replaces email + phone
  
  @Column(nullable = false, length = 255)
  private String passwordHash;  // MODIFIED: replaces plaintext password
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(updatable = false)
  private LocalDateTime createdAt;  // NEW FIELD
  
  @Temporal(TemporalType.TIMESTAMP)
  private LocalDateTime updatedAt;  // NEW FIELD
  
  @PrePersist
  protected void onCreate() {
	if (createdAt == null) createdAt = LocalDateTime.now();
	if (updatedAt == null) updatedAt = LocalDateTime.now();
  }
  
  @PreUpdate
  protected void onUpdate() {
	updatedAt = LocalDateTime.now();
  }
}
```

#### Session.java (NEW Entity)
```java
@Entity
@Table(name = "sessions")
public class Session {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String sessionId;
  
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  
  @Column(nullable = false)
  private Boolean isActive;  // default true
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(updatable = false)
  private LocalDateTime createdAt;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private LocalDateTime expiresAt;
  
  @PrePersist
  protected void onCreate() {
	if (createdAt == null) createdAt = LocalDateTime.now();
	if (isActive == null) isActive = true;
	if (expiresAt == null) expiresAt = LocalDateTime.now().plusDays(30);
  }
}
```

### Repository Methods

#### UserRepository.java (NEW methods)
```java
public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByloginId(String loginId);
  boolean existsByloginId(String loginId);
}
```

#### SessionRepository.java (NEW)
```java
public interface SessionRepository extends JpaRepository<Session, String> {
  Optional<Session> findBySessionIdAndIsActiveTrueAndExpiresAtAfter(
	String sessionId, 
	LocalDateTime now
  );
  
  List<Session> findByUserIdAndIsActive(String userId, Boolean isActive);
  
  void deleteByExpiresAtBefore(LocalDateTime now);  // For cleanup job (future)
}
```

## Implementation Details

### Files to Create/Modify
- `User.java` - Update entity (modify fields)
- `Session.java` - NEW entity
- `UserRepository.java` - Update/create repository
- `SessionRepository.java` - NEW repository
- `application.properties` - Ensure JPA DDL-auto set correctly
- Database migration scripts (if using Liquibase/Flyway; optional for MVP)

### SQL Schema (H2/PostgreSQL)

**users table:**
```sql
CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,
  loginId VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_loginId ON users(loginId);
```

**sessions table:**
```sql
CREATE TABLE sessions (
  session_id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_sessions_session_id ON sessions(session_id);
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_is_active ON sessions(is_active);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);
```

### Hibernate Configuration (application.properties)
```properties
# Dev/Test: auto generate schema
spring.jpa.hibernate.ddl-auto=create-drop

# Prod: validate only (use migrations)
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL10Dialect
```

### Testing Checklist
- [ ] Unit test: User entity with loginId field
- [ ] Unit test: Session entity with expiry calculation
- [ ] Integration test: Save User and retrieve by loginId
- [ ] Integration test: Create Session and validate is_active
- [ ] Integration test: Query active sessions for user
- [ ] Integration test: Soft delete (is_active = false)
- [ ] Integration test: Unique constraint on loginId enforced
- [ ] Database test: Foreign key constraint enforced
- [ ] Database test: Indexes created
- [ ] Migration test: Existing data preserved (if applicable)

## Edge Cases
1. loginId update - Should update both table and indices
2. Password hash change - Should not trigger updated_at on read operations
3. Concurrent session creation - Should handle race condition (DB unique constraint)
4. Session expiry at exact timestamp - Use <= comparison (already handled by `ExpiresAtAfter` query)
5. Timezone handling - Use UTC timestamps in DB (LocalDateTime in Java)

## Backward Compatibility
- If existing User table has separate email/phone fields:
  - Create migration script to consolidate
  - Back up data first
  - Migrate email column → loginId (keep data)
  - Test migration on staging environment first

## Performance
- Create indices on frequently queried columns (loginId, sessionId, userId)
- Monitor query performance in production (no full table scans)

## Dependencies
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `jakarta.persistence:jakarta.persistence-api`

## Future Enhancements (not in MVP)
- Add email_verified_at, phone_verified_at timestamps
- Add last_login_at timestamp
- Add session metadata (IP address, device type, user agent)
- Implement session cleanup job (delete expired sessions)
- Add audit logging table

## Definition of Done
- [ ] User.java updated with new fields
- [ ] Session.java created with correct structure
- [ ] Repositories updated with query methods
- [ ] Database schema created/migrated
- [ ] All indices created
- [ ] Tests passing (integration + unit)
- [ ] Data migration successful (if applicable)
- [ ] Code reviewed
- [ ] Merged to develop branch


