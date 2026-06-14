#!/bin/bash
# ══════════════════════════════════════════════════════════
# JIRAMA CRM — Development Startup Script
# ══════════════════════════════════════════════════════════
# Usage:
#   ./start.sh            # Full stack (build + up)
#   ./start.sh quick      # Skip build, just up
#   ./start.sh down       # Tear down everything
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

    *)
        echo -e "${RED}Unknown command: $1${NC}"
        echo "Usage: ./start.sh [full|quick|down|logs]"
        exit 1
        ;;
esac
