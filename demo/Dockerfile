# ==========================================
# Stage 1: Build Stage
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven Wrapper and configuration
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Give execution permission
RUN chmod +x mvnw

# Download dependencies first (Docker cache optimization)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build Spring Boot application
RUN ./mvnw clean package -DskipTests -B


# ==========================================
# Stage 2: Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy executable JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Set file ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Spring Boot application port
EXPOSE 8080

# JVM options and application startup
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
