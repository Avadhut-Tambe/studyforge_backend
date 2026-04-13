#!/usr/bin/env bash
# ============================================================
# BookStore — Local Quick Start Script
# Run from project root: bash start-local.sh
# ============================================================
set -e

BACKEND_DIR="$(pwd)/bookstore-backend"
FRONTEND_DIR="$(pwd)/bookstore-frontend"

echo "=========================================="
echo "  BookStore — Local Development Startup"
echo "=========================================="
echo ""

# Check prerequisites
command -v java  >/dev/null 2>&1 || { echo "ERROR: Java 17+ is required"; exit 1; }
command -v mvn   >/dev/null 2>&1 || { echo "ERROR: Maven is required";    exit 1; }
command -v node  >/dev/null 2>&1 || { echo "ERROR: Node.js 20+ is required"; exit 1; }
command -v npm   >/dev/null 2>&1 || { echo "ERROR: npm is required";      exit 1; }

# Check Firebase config
if [ -z "$FIREBASE_PROJECT_ID" ]; then
  echo "WARNING: FIREBASE_PROJECT_ID is not set."
  echo "  Please export FIREBASE_PROJECT_ID=your-project-id"
  echo ""
fi

if [ -z "$JWT_SECRET" ]; then
  export JWT_SECRET="bookstore-super-secret-jwt-key-minimum-256-bits-long-for-hs256-algorithm"
  echo "INFO: Using default JWT_SECRET (change in production!)"
fi

# Build backend (skip if already built)
echo ""
echo "[1/3] Building backend..."
cd "$BACKEND_DIR"
mvn clean package -DskipTests -q
echo "      Backend build complete."

# Start all backend services in background
echo ""
echo "[2/3] Starting backend microservices..."
SERVICES=("auth-service" "user-service" "book-service" "order-service" "payment-service")
PIDS=()

for svc in "${SERVICES[@]}"; do
  echo "      Starting $svc..."
  cd "$BACKEND_DIR/$svc"
  mvn spring-boot:run -q &
  PIDS+=($!)
  sleep 1
done

# Start gateway last
echo "      Starting api-gateway..."
cd "$BACKEND_DIR/api-gateway"
mvn spring-boot:run -q &
PIDS+=($!)

echo "      Waiting 20s for services to initialize..."
sleep 20

# Start frontend
echo ""
echo "[3/3] Starting frontend..."
cd "$FRONTEND_DIR"
if [ ! -d "node_modules" ]; then
  echo "      Installing npm dependencies..."
  npm install -q
fi
npm run dev &
PIDS+=($!)

echo ""
echo "=========================================="
echo "  All services started!"
echo "=========================================="
echo "  Frontend:        http://localhost:3000"
echo "  API Gateway:     http://localhost:8080"
echo "  Auth Service:    http://localhost:8081"
echo "  User Service:    http://localhost:8082"
echo "  Book Service:    http://localhost:8083"
echo "  Order Service:   http://localhost:8084"
echo "  Payment Service: http://localhost:8085"
echo ""
echo "  Press Ctrl+C to stop all services"
echo "=========================================="

# Wait for Ctrl+C
trap 'echo "Stopping all services..."; kill "${PIDS[@]}" 2>/dev/null; exit 0' SIGINT SIGTERM
wait
