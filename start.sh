#!/bin/bash
# ══════════════════════════════════════════════════════════
# JIRAMA CRM — Development Startup Script
# ══════════════════════════════════════════════════════════
# Usage:
#   ./start.sh                     # Full stack (build + up)
#   ./start.sh quick               # Skip build, just up
#   ./start.sh down                # Tear down everything
#   ./start.sh local               # Infra in Docker, app on host (hot reload)
#   ./start.sh backend             # Run backend tests (with Docker Postgres)
#   ./start.sh backend:tc          # Run backend tests (TestContainers)
#   ./start.sh backend:unit        # Run backend unit tests only (no Docker)
#   ./start.sh frontend [command]  # Run frontend tests/command
# ══════════════════════════════════════════════════════════

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔══════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     JIRAMA CRM — Development Mode    ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════╝${NC}"

# Check .env exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}⚠  No .env file found. Creating from .env.example...${NC}"
    cp .env.example .env
    echo -e "${YELLOW}⚠  Edit .env with your passwords before running in production!${NC}"
fi

# Check SSL certs exist
if [ ! -f docker/nginx/ssl/cert.pem ]; then
    echo -e "${YELLOW}⚠  No SSL certificates found. Generating self-signed certs...${NC}"
    bash docker/nginx/scripts/generate-certs.sh
fi

case "${1:-full}" in
    full)
        echo -e "\n${GREEN}→ Building frontend and starting all services...${NC}"
        docker compose --profile build up -d --build
        echo -e "\n${GREEN}✓ Services started!${NC}"
        echo -e "  Frontend:  http://localhost"
        echo -e "  Backend:   http://localhost/api"
        echo -e "  Keycloak:  http://localhost/auth"
        echo -e "  MinIO:     http://localhost:9001"
        echo -e "  Grafana:   http://localhost:3000"
        echo -e "  Prometheus: http://localhost:9090"
        echo -e "\n${YELLOW}  Default credentials:${NC}"
        echo -e "  Keycloak admin: admin / keycloak_admin_password_change_me"
        echo -e "  Grafana:        admin / grafana_password_change_me"
        echo -e "\n  ${BLUE}Run './start.sh logs' to see logs${NC}"
        ;;

    quick)
        echo -e "\n${GREEN}→ Starting services without rebuilding frontend...${NC}"
        docker compose up -d
        echo -e "\n${GREEN}✓ Services started (quick mode)${NC}"
        ;;

    local)
        echo -e "\n${GREEN}→ Starting infrastructure services (postgres, redis, minio, keycloak)...${NC}"
        docker compose up -d postgres redis minio keycloak

        echo -e "\n${YELLOW}→ Waiting for Postgres to be healthy...${NC}"
        until docker compose exec -T postgres pg_isready -U jirama_app -d jirama > /dev/null 2>&1; do
            sleep 2
        done
        echo -e "${GREEN}✓ Postgres is ready${NC}"

        echo -e "\n${YELLOW}→ Waiting for Keycloak to be healthy...${NC}"
        until curl -sf http://localhost:8081/health/ready > /dev/null 2>&1; do
            sleep 5
        done
        echo -e "${GREEN}✓ Keycloak is ready${NC}"

        # Start backend in background
        echo -e "\n${GREEN}→ Starting backend (Maven hot reload)...${NC}"
        cd backend && mvn spring-boot:run &
        BACKEND_PID=$!
        cd "$SCRIPT_DIR"

        # Cleanup on exit
        _local_cleaned=0
        cleanup() {
            [ "$_local_cleaned" -eq 1 ] && return
            _local_cleaned=1
            echo -e "\n${YELLOW}→ Stopping backend (PID $BACKEND_PID)...${NC}"
            kill "$BACKEND_PID" 2>/dev/null || true
            wait "$BACKEND_PID" 2>/dev/null || true
            echo -e "${GREEN}✓ Backend stopped${NC}"
            echo -e "\n${YELLOW}ℹ Docker services are still running. Stop them with:${NC}"
            echo -e "  ./start.sh down"
        }
        trap cleanup EXIT INT TERM

        # Start frontend in foreground (Vite HMR)
        echo -e "\n${GREEN}→ Starting frontend (Vite HMR)...${NC}"
        echo -e "  ${YELLOW}Press Ctrl+C to stop everything${NC}\n"
        cd frontend && npm run dev
        cd "$SCRIPT_DIR"
        ;;

    down)
        echo -e "\n${YELLOW}→ Stopping all services...${NC}"
        docker compose down --remove-orphans
        echo -e "\n${GREEN}✓ Services stopped. Volumes preserved.${NC}"
        echo -e "  Use 'docker compose down -v' to also delete data volumes."
        ;;

    logs)
        echo -e "\n${GREEN}→ Following all logs...${NC}"
        docker compose logs -f
        ;;

    backend)
        echo -e "\n${GREEN}→ Running backend tests with Docker Postgres service...${NC}"
        docker compose up -d postgres
        cd backend && mvn test --batch-mode -e \
            -Dspring.profiles.active=test \
            -Dspring.datasource.url=jdbc:postgresql://localhost:5432/jirama_test \
            -Dspring.datasource.username=test \
            -Dspring.datasource.password=test \
            -Dspring.datasource.driver-class-name=org.postgresql.Driver
        cd "$SCRIPT_DIR"
        ;;

    backend:tc)
        echo -e "\n${GREEN}→ Running backend tests with TestContainers (self-managed Postgres)...${NC}"
        cd backend && mvn test --batch-mode -e -Dspring.profiles.active=test
        cd "$SCRIPT_DIR"
        ;;

    backend:unit)
        echo -e "\n${GREEN}→ Running backend unit tests only (no Docker required)...${NC}"
        cd backend && mvn test --batch-mode -e -Pno-docker -Dspring.profiles.active=test
        cd "$SCRIPT_DIR"
        ;;

    frontend)
        CMD="${2:-all}"
        echo -e "\n${GREEN}→ Running frontend command: ${CMD}${NC}"
        cd frontend
        if [ "$CMD" = "all" ]; then
            npm run typecheck
            npm test
            npm run lint
        else
            npm run "$CMD"
        fi
        cd "$SCRIPT_DIR"
        ;;

    *)
        echo -e "${RED}Unknown command: $1${NC}"
        echo "Usage: ./start.sh [full|quick|down|logs|local|backend|backend:tc|backend:unit|frontend]"
        exit 1
        ;;
esac
