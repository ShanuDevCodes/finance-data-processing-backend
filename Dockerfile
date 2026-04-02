# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper and dependency declarations first (layer cache)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached unless build files change)
RUN ./gradlew dependencies --no-daemon -q || true

# Copy source and build the fat JAR, skipping tests (tests run in CI / locally)
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copy only the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Render injects $PORT at runtime; default to 8081 locally
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "-Dserver.port=${PORT:-8081}", "app.jar"]
