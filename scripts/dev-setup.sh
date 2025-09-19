#!/bin/bash

# Development Environment Setup Script for School Scheduling System

set -e

echo "🚀 Setting up development environment for School Scheduling System..."

# Check if Java 17+ is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17+ and try again."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17+ is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java $JAVA_VERSION detected"

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18+ and try again."
    exit 1
fi

NODE_VERSION=$(node --version | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ Node.js 18+ is required. Current version: $NODE_VERSION"
    exit 1
fi

echo "✅ Node.js $(node --version) detected"

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "⚠️  Docker is not installed. You can still develop locally but Docker features won't work."
else
    echo "✅ Docker $(docker --version) detected"
fi

# Setup backend
echo "📦 Setting up backend..."
cd backend

# Create logs directory
mkdir -p logs

# Install backend dependencies
echo "Installing Maven dependencies..."
./mvnw clean install -DskipTests

cd ..

# Setup frontend
echo "📦 Setting up frontend..."
cd frontend

# Install frontend dependencies
echo "Installing npm dependencies..."
npm ci

cd ..

# Create environment files
echo "⚙️  Creating environment configuration..."

# Backend environment
cat > backend/.env << EOF
SPRING_PROFILES_ACTIVE=dev
DB_HOST=localhost
DB_PORT=3306
DB_NAME=school_scheduling
DB_USERNAME=root
DB_PASSWORD=password
JWT_SECRET=dev-secret-key-change-in-production
EOF

# Frontend environment
cat > frontend/.env << EOF
VITE_API_URL=http://localhost:8080/api
VITE_APP_TITLE=School Scheduling System
VITE_APP_VERSION=1.0.0
EOF

echo "✅ Environment configuration created"

# Create .gitignore files
echo "📝 Creating .gitignore files..."

cat > .gitignore << EOF
# Dependencies
node_modules/
frontend/node_modules/
backend/target/
backend/.mvn/wrapper/maven-wrapper.jar

# Environment variables
.env
*.env
*.env.local
*.env.production

# Logs
logs/
*.log
backend/logs/

# IDE
.idea/
.vscode/
*.swp
*.swo

# OS
.DS_Store
Thumbs.db

# Docker
.dockerignore

# Build artifacts
frontend/dist/
frontend/build/

# Test coverage
coverage/
.nyc_output/

# Temporary files
*.tmp
*.temp
.tmp/
.temp/

# Database
*.db
*.sqlite
*.sqlite3

# Redis dump
dump.rdb
EOF

cat > frontend/.gitignore << EOF
# Dependencies
node_modules/
.pnp
.pnp.js

# Testing
coverage/

# Production
dist/
build/

# Environment files
.env
.env.local
.env.development.local
.env.test.local
.env.production.local

# IDE
.idea/
.vscode/
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?
EOF

cat > backend/.gitignore << EOF
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iws
*.iml
*.ipr
.vscode/

# OS
.DS_Store
Thumbs.db

# Logs
logs/
*.log

# Environment
.env
*.env

# Test coverage
target/site/jacoco/
target/site/serenity/
target/coverage/
EOF

echo "✅ .gitignore files created"

echo ""
echo "🎉 Development environment setup complete!"
echo ""
echo "Next steps:"
echo "1. Start MySQL database: docker run --name school-mysql -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=school_scheduling -p 3306:3306 -d mysql:8.0"
echo "2. Start backend: cd backend && ./mvnw spring-boot:run"
echo "3. Start frontend: cd frontend && npm run dev"
echo "4. Open http://localhost:3000 in your browser"
echo ""
echo "Default login: admin / admin123"