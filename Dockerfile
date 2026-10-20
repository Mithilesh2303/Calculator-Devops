# Stage 1: Build Stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Leverage Docker layer caching by copying pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code and build executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime Stage
FROM eclipse-temurin:17-jre-jammy

# Create non-root user and group for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# Copy packaged JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Set ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose Spring Boot port
EXPOSE 8080

# Health check configuration pointing to Actuator probe
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Launch Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
