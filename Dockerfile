# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Cache Gradle wrapper and dependencies
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

# Build the application
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy the fat jar produced by bootJar (archiveFileName = app.jar)
COPY --from=build /app/build/libs/app.jar app.jar

# Render injects the PORT env var (default 10000); bind Spring Boot to it.
ENV PORT=10000
EXPOSE 10000

# Use exec form via sh so $PORT is expanded at runtime.
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar app.jar --server.port=${PORT}"]
