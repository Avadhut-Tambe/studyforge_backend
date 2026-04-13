# ============================================================
# BookStore — Local Quick Start (PowerShell for Windows)
# Run: powershell -ExecutionPolicy Bypass -File start-local.ps1
# ============================================================

$ErrorActionPreference = "Stop"

$BackendDir  = "$PSScriptRoot\bookstore-backend"
$FrontendDir = "$PSScriptRoot\bookstore-frontend"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  BookStore — Local Development Startup"   -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Set environment variables if not already set
if (-not $env:FIREBASE_PROJECT_ID) {
    Write-Host "WARNING: FIREBASE_PROJECT_ID not set. Set it before running!" -ForegroundColor Yellow
    $env:FIREBASE_PROJECT_ID = "studyforge-8d9b6"
}
if (-not $env:JWT_SECRET) {
    $env:JWT_SECRET = "bookstore-super-secret-jwt-key-minimum-256-bits-long-for-hs256-algorithm"
    Write-Host "INFO: Using default JWT_SECRET" -ForegroundColor Yellow
}

# Build backend
Write-Host "[1/3] Building backend (this may take a minute)..." -ForegroundColor Green
Set-Location $BackendDir
& mvn clean package -DskipTests -q
Write-Host "      Build complete." -ForegroundColor Green

# Function to open a new terminal for each service
function Start-Service($name, $port) {
    $dir = "$BackendDir\$name"
    Start-Process powershell -ArgumentList "-NoExit", "-Command",
        "cd '$dir'; `$env:FIREBASE_PROJECT_ID='$env:FIREBASE_PROJECT_ID'; `$env:JWT_SECRET='$env:JWT_SECRET'; mvn spring-boot:run" `
        -WindowStyle Normal
    Write-Host "      Started $name on port $port" -ForegroundColor Gray
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "[2/3] Starting backend microservices..." -ForegroundColor Green
Start-Service "auth-service"    8081
Start-Service "user-service"    8082
Start-Service "book-service"    8083
Start-Service "order-service"   8084
Start-Service "payment-service" 8085

Write-Host "      Waiting 15s for services to initialize..."
Start-Sleep -Seconds 15
Start-Service "api-gateway"     8080
Start-Sleep -Seconds 5

# Start frontend
Write-Host ""
Write-Host "[3/3] Starting frontend..." -ForegroundColor Green
Set-Location $FrontendDir
if (-not (Test-Path "node_modules")) {
    Write-Host "      Installing npm dependencies..."
    & npm install
}
Start-Process powershell -ArgumentList "-NoExit", "-Command",
    "cd '$FrontendDir'; npm run dev" -WindowStyle Normal

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  All services started!" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Frontend:    http://localhost:3000" -ForegroundColor White
Write-Host "  Gateway:     http://localhost:8080" -ForegroundColor White
Write-Host ""
Write-Host "  Each service is running in its own terminal window." -ForegroundColor Yellow
Write-Host "  Close all windows to stop the application." -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
