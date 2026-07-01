# Multi-stage build to create a minimal Docker image
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Grant execute permissions to Gradle wrapper and gradle directory
RUN chmod +x gradlew && chmod -R +x gradle/wrapper

# Debug: List files in app directory and gradle wrapper directory
RUN echo "=== Files in /app ===" && ls -la /app
RUN echo "=== Files in /app/gradle ===" && ls -la /app/gradle
RUN echo "=== Files in /app/gradle/wrapper ===" && ls -la /app/gradle/wrapper
RUN echo "=== Checking if gradle-wrapper.jar exists ===" && test -f /app/gradle/wrapper/gradle-wrapper.jar && echo "gradle-wrapper.jar FOUND" || echo "gradle-wrapper.jar NOT FOUND"

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