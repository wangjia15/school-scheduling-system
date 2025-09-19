# Development Environment Setup Script for School Scheduling System (Windows)

Write-Host "🚀 Setting up development environment for School Scheduling System..." -ForegroundColor Green

# Check if Java is installed
try {
    $javaVersion = java -version 2>&1 | Select-String -Pattern "version" | ForEach-Object { $_ -split '"' | Select-Object -Index 1 }
    $javaMajorVersion = $javaVersion.Split('.')[0]
    if ($javaMajorVersion -lt 17) {
        Write-Host "❌ Java 17+ is required. Current version: $javaMajorVersion" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ Java $javaVersion detected" -ForegroundColor Green
} catch {
    Write-Host "❌ Java is not installed. Please install Java 17+ and try again." -ForegroundColor Red
    exit 1
}

# Check if Node.js is installed
try {
    $nodeVersion = node --version
    $nodeMajorVersion = $nodeVersion.Substring(1).Split('.')[0]
    if ($nodeMajorVersion -lt 18) {
        Write-Host "❌ Node.js 18+ is required. Current version: $nodeMajorVersion" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ Node.js $nodeVersion detected" -ForegroundColor Green
} catch {
    Write-Host "❌ Node.js is not installed. Please install Node.js 18+ and try again." -ForegroundColor Red
    exit 1
}

# Check if Docker is installed
try {
    $dockerVersion = docker --version
    Write-Host "✅ Docker $dockerVersion detected" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Docker is not installed. You can still develop locally but Docker features won't work." -ForegroundColor Yellow
}

# Setup backend
Write-Host "📦 Setting up backend..." -ForegroundColor Blue
Set-Location backend

# Create logs directory
New-Item -ItemType Directory -Force -Path logs

# Install backend dependencies
Write-Host "Installing Maven dependencies..." -ForegroundColor Blue
.\mvnw clean install -DskipTests

Set-Location ..

# Setup frontend
Write-Host "📦 Setting up frontend..." -ForegroundColor Blue
Set-Location frontend

# Install frontend dependencies
Write-Host "Installing npm dependencies..." -ForegroundColor Blue
npm ci

Set-Location ..

# Create environment files
Write-Host "⚙️  Creating environment configuration..." -ForegroundColor Blue

# Backend environment
@"
SPRING_PROFILES_ACTIVE=dev
DB_HOST=localhost
DB_PORT=3306
DB_NAME=school_scheduling
DB_USERNAME=root
DB_PASSWORD=password
JWT_SECRET=dev-secret-key-change-in-production
"@ | Out-File -FilePath "backend\.env" -Encoding UTF8

# Frontend environment
@"
VITE_API_URL=http://localhost:8080/api
VITE_APP_TITLE=School Scheduling System
VITE_APP_VERSION=1.0.0
"@ | Out-File -FilePath "frontend\.env" -Encoding UTF8

Write-Host "✅ Environment configuration created" -ForegroundColor Green

# Create .gitignore files
Write-Host "📝 Creating .gitignore files..." -ForegroundColor Blue

@"
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
"@ | Out-File -FilePath ".gitignore" -Encoding UTF8

@"
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
"@ | Out-File -FilePath "frontend\.gitignore" -Encoding UTF8

@"
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
"@ | Out-File -FilePath "backend\.gitignore" -Encoding UTF8

Write-Host "✅ .gitignore files created" -ForegroundColor Green

Write-Host ""
Write-Host "🎉 Development environment setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Start MySQL database: docker run --name school-mysql -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=school_scheduling -p 3306:3306 -d mysql:8.0" -ForegroundColor Yellow
Write-Host "2. Start backend: cd backend && .\mvnw spring-boot:run" -ForegroundColor Yellow
Write-Host "3. Start frontend: cd frontend && npm run dev" -ForegroundColor Yellow
Write-Host "4. Open http://localhost:3000 in your browser" -ForegroundColor Yellow
Write-Host ""
Write-Host "Default login: admin / admin123" -ForegroundColor Yellow