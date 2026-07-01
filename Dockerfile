# Multi-stage build to create a minimal Docker image
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Grant execute permission to the Gradle wrapper
RUN chmod +x gradlew

# Copy module source code
COPY api api/
COPY qa qa/

# Build the API module
RUN ./gradlew :api:bootJar --no-daemon

# Create final minimal image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built JAR from the API module builder stage
COPY --from=builder /app/api/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]