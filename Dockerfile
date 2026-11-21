# Stage 1: Build stage
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Copy Gradle wrapper and configuration files
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Ensure Gradle wrapper has execution permissions
RUN chmod +x ./gradlew

# Copy source code
COPY src src

# Package executable bootJar without running tests
RUN ./gradlew bootJar -x test --no-daemon && \
    cp $(ls build/libs/*.jar | grep -v plain) app.jar

# Stage 2: Minimal runtime stage
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy compiled jar from build stage
COPY --from=builder /app/app.jar app.jar

# Expose server port (Render dynamically sets PORT env var)
ENV PORT=8080
EXPOSE ${PORT}

# Run with container-aware memory limits suitable for Render Free Tier (512MB RAM)
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -jar app.jar"]
