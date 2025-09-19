# Backend stage
FROM eclipse-temurin:17-jdk-alpine AS backend

WORKDIR /app

# Copy backend pom.xml and download dependencies
COPY backend/pom.xml backend/pom.xml
COPY backend/.mvn/backend/.mvn
COPY backend/mvnw backend/mvnw
COPY backend/.mvn/wrapper/maven-wrapper.jar backend/.mvn/wrapper/maven-wrapper.jar
COPY backend/.mvn/wrapper/maven-wrapper.properties backend/.mvn/wrapper/maven-wrapper.properties

RUN cd backend && ./mvnw dependency:go-offline -B

# Copy backend source code
COPY backend/src backend/src

# Build the backend
RUN cd backend && ./mvnw clean package -DskipTests

# Frontend stage
FROM node:18-alpine AS frontend

WORKDIR /app

# Copy frontend package files
COPY frontend/package*.json ./

# Install frontend dependencies
RUN npm ci

# Copy frontend source code
COPY frontend/. .

# Build the frontend
RUN npm run build

# Final stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install required packages
RUN apk add --no-cache curl

# Copy backend jar
COPY --from=backend /app/backend/target/*.jar app.jar

# Copy frontend build
COPY --from=frontend /app/dist /app/static

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -S appuser -u 1001

# Change ownership
RUN chown -R appuser:appgroup /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]