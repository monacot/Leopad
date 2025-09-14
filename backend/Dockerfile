# Multi-stage build for Spring Boot application

# Stage 1: Build the application
FROM maven:3-openjdk-17 AS build
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build the application (Maven is already installed in this image)
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/notepad-app-*.jar app.jar

# Expose port
EXPOSE 8080

# Set JVM options for Cloud Run
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check using curl (install curl first)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]