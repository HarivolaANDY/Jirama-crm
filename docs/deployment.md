# Docker Deployment Plan

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Docker Compose (Single Host / Swarm)         │
│                                                                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────────────┐ │
│  │  Nginx    │  │ Frontend │  │ Backend  │  │   Keycloak         │ │
│  │ :443/80   │◄─│ :5173    │◄─│ :8080    │  │   :8081            │ │
│  │ (Reverse) │  │ (Static) │  │ (API)    │  │   (IAM)            │ │
│  └──────────┘  └──────────┘  └────┬─────┘  └────────────────────┘ │
│                                    │                                │
│  ┌──────────┐  ┌──────────┐  ┌────▼─────┐  ┌────────────────────┐ │
│  │PostgreSQL│  │  Redis   │  │  MinIO   │  │  Grafana/Prometheus │ │
│  │ :5432    │  │ :6379    │  │ :9000    │  │  :3000/:9090        │ │
│  └──────────┘  └──────────┘  └──────────┘  └────────────────────┘ │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## Docker Compose Configuration

```yaml
version: '3.9'

services:
  # ── Reverse Proxy ──
  nginx:
    image: nginx:1.27-alpine
    container_name: jirama-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./docker/nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./docker/nginx/sites:/etc/nginx/sites-enabled:ro
      - ./docker/nginx/ssl:/etc/nginx/ssl:ro
      - static_data:/var/www/static:ro
    depends_on:
      - frontend
      - backend
    networks:
      - jirama_net
    restart: unless-stopped

  # ── Frontend (Static Files Builder) ──
  frontend:
    image: node:22-alpine
    container_name: jirama-frontend-builder
    working_dir: /app
    volumes:
      - ./frontend:/app
      - /app/node_modules
      - static_data:/app/dist
    command: sh -c "pnpm install && pnpm build"
    networks:
      - jirama_net
    profiles:
      - build

  # ── Backend API ──
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: jirama-backend
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILE:-prod}
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/jirama
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASS}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      SPRING_REDIS_PASSWORD: ${REDIS_PASS}
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: https://keycloak:8081/realms/jirama
      SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE: 50MB
      MINIO_ENDPOINT: http://minio:9000
      MINIO_ACCESS_KEY: ${MINIO_ACCESS_KEY}
      MINIO_SECRET_KEY: ${MINIO_SECRET_KEY}
      MINIO_BUCKET: jirama-documents
      SERVER_SERVLET_CONTEXT_PATH: /api
      APP_BASE_URL: https://jirama.mg
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_started
      minio:
        condition: service_started
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    volumes:
      - backend_logs:/var/log/app
    networks:
      - jirama_net
    restart: unless-stopped

  # ── Keycloak (IAM) ──
  keycloak:
    image: quay.io/keycloak/keycloak:25.0
    container_name: jirama-keycloak
    command: start --import-realm
    environment:
      KC_HOSTNAME: auth.jirama.mg
      KC_HTTP_PORT: 8081
      KC_DB: postgres
      KC_DB_URL_HOST: postgres
      KC_DB_URL_DATABASE: keycloak
      KC_DB_USERNAME: ${KEYCLOAK_DB_USER}
      KC_DB_PASSWORD: ${KEYCLOAK_DB_PASS}
      KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN_USER}
      KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASS}
      KC_HTTPS_CERTIFICATE_FILE: /etc/x509/https/tls.crt
      KC_HTTPS_CERTIFICATE_KEY_FILE: /etc/x509/https/tls.key
      KC_PROXY: edge
    volumes:
      - ./docker/keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json:ro
      - ./docker/nginx/ssl:/etc/x509/https:ro
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - jirama_net
    restart: unless-stopped

  # ── PostgreSQL ──
  postgres:
    image: postgres:16-alpine
    container_name: jirama-postgres
    environment:
      POSTGRES_MULTIPLE_DATABASES: jirama,keycloak
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASS}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init-multiple-dbs.sh:/docker-entrypoint-initdb.d/init.sh:ro
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER} -d jirama"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - jirama_net
    restart: unless-stopped

  # ── Redis ──
  redis:
    image: redis:7-alpine
    container_name: jirama-redis
    command: redis-server --requirepass ${REDIS_PASS} --appendonly yes --maxmemory 2gb --maxmemory-policy allkeys-lru
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 10s
      timeout: 5s
      retries: 3
    networks:
      - jirama_net
    restart: unless-stopped

  # ── MinIO (Object Storage) ──
  minio:
    image: minio/minio:latest
    container_name: jirama-minio
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_ACCESS_KEY}
      MINIO_ROOT_PASSWORD: ${MINIO_SECRET_KEY}
    volumes:
      - minio_data:/data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 10s
      retries: 3
    networks:
      - jirama_net
    restart: unless-stopped

  # ── Prometheus ──
  prometheus:
    image: prom/prometheus:latest
    container_name: jirama-prometheus
    volumes:
      - ./docker/monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=30d'
    networks:
      - jirama_net
    restart: unless-stopped

  # ── Grafana ──
  grafana:
    image: grafana/grafana:latest
    container_name: jirama-grafana
    environment:
      GF_SECURITY_ADMIN_USER: ${GRAFANA_ADMIN_USER}
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_ADMIN_PASS}
      GF_INSTALL_PLUGINS: grafana-piechart-panel
    volumes:
      - ./docker/monitoring/grafana-datasources.yml:/etc/grafana/provisioning/datasources/datasources.yml:ro
      - ./docker/monitoring/grafana-dashboards:/etc/grafana/provisioning/dashboards:ro
      - grafana_data:/var/lib/grafana
    networks:
      - jirama_net
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:
  minio_data:
  prometheus_data:
  grafana_data:
  backend_logs:
  static_data:

networks:
  jirama_net:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

## Nginx Configuration

```nginx
# /etc/nginx/nginx.conf
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 4096;
    multi_accept on;
    use epoll;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    # Security
    server_tokens off;
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'nonce-${csp_nonce}'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self' https://keycloak.jirama.mg;";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
    
    # SSL
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # Performance
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    client_max_body_size 50M;
    
    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=100r/s;
    limit_req_zone $binary_remote_addr zone=auth:10m rate=20r/m;
    
    # Gzip
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
    gzip_min_length 1000;
    
    # Logging
    log_format json escape=json '{'
        '"time":"$time_iso8601",'
        '"remote_addr":"$remote_addr",'
        '"request":"$request",'
        '"status":$status,'
        '"body_bytes":$body_bytes_sent,'
        '"request_time":$request_time,'
        '"upstream_addr":"$upstream_addr",'
        '"http_referrer":"$http_referer",'
        '"http_user_agent":"$http_user_agent"'
    '}';
    access_log /var/log/nginx/access.log json;
    
    # Server block
    server {
        listen 80;
        server_name jirama.mg *.jirama.mg;
        return 301 https://$host$request_uri;
    }
    
    server {
        listen 443 ssl http2;
        server_name jirama.mg;
        
        ssl_certificate /etc/nginx/ssl/fullchain.pem;
        ssl_certificate_key /etc/nginx/ssl/privkey.pem;
        
        # Frontend static files
        root /var/www/static;
        index index.html;
        
        # SPA — all non-file routes serve index.html
        location / {
            try_files $uri $uri/ /index.html;
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
        
        # Service worker — no cache
        location /sw.js {
            add_header Cache-Control "no-cache";
        }
        
        # API proxy
        location /api/ {
            limit_req zone=api burst=200 nodelay;
            proxy_pass http://backend:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # Increase timeout for long-running requests (reports)
            proxy_read_timeout 120s;
            proxy_send_timeout 30s;
        }
        
        # Keycloak
        location /auth/ {
            limit_req zone=auth burst=10 nodelay;
            proxy_pass http://keycloak:8081;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
        
        # Health check
        location /health {
            access_log off;
            return 200 "healthy\n";
        }
        
        # Deny access to hidden files
        location ~ /\. {
            deny all;
            access_log off;
            log_not_found off;
        }
    }
}
```

## Docker Backend Dockerfile

```dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# Runtime
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

RUN apk add --no-cache curl

USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -sf http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseZGC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
```

## CI/CD Pipeline (GitHub Actions)

```yaml
name: Build & Deploy

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: jirama_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports:
          - 5432:5432
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Backend Tests
        run: |
          cd backend
          ./mvnw verify -B
      
      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '22'
      
      - name: Frontend Tests
        run: |
          cd frontend
          pnpm install
          pnpm lint
          pnpm test
          pnpm build

  docker-build:
    needs: test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3
      
      - name: Login to Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      
      - name: Build & Push Backend
        uses: docker/build-push-action@v5
        with:
          context: ./backend
          push: true
          tags: ghcr.io/jirama/backend:${{ github.sha }},ghcr.io/jirama/backend:latest
          cache-from: type=gha
          cache-to: type=gha,mode=max

  deploy:
    needs: docker-build
    runs-on: ubuntu-latest
    environment: production
    
    steps:
      - name: Deploy to Production
        uses: appleboy/ssh-action@v1.0.0
        with:
          host: ${{ secrets.DEPLOY_HOST }}
          username: ${{ secrets.DEPLOY_USER }}
          key: ${{ secrets.DEPLOY_KEY }}
          script: |
            cd /opt/jirama
            docker compose pull backend
            docker compose up -d backend --no-deps
            docker image prune -f
```

## Monitoring & Alerting

### Grafana Dashboards

| Dashboard | Description |
|---|---|
| **JIRAMA Operations** | Active subscribers, revenue, collection rate, outage counts |
| **API Performance** | Request latency (p50/p95/p99), error rates, throughput |
| **Database** | Connection pool, query performance, cache hit ratio |
| **Infrastructure** | CPU, memory, disk I/O, network for all containers |
| **Application Logs** | Error rates by endpoint, top errors, user actions |

### Prometheus Metrics (Spring Boot Actuator)

```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: jirama-backend
  endpoint:
    prometheus:
      enabled: true
```

### Alerts (Prometheus + Alertmanager)

| Alert | Condition | Channel |
|---|---|---|
| High API Error Rate | `error_rate > 5% in 5min` | Email + Slack |
| High Latency | `p95_latency > 2s for 5min` | Email + Slack |
| DB Connection Pool Exhaustion | `pool_active > 80% for 2min` | SMS + Slack |
| Disk Space Low | `disk_free < 10%` | SMS |
| Service Down | `up == 0` | SMS (PagerDuty) |

## Environment Variables (.env)

```bash
# Database
DB_USER=jirama_app
DB_PASS=<generate-strong-password>

# Keycloak
KEYCLOAK_DB_USER=keycloak
KEYCLOAK_DB_PASS=<generate-strong-password>
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASS=<generate-strong-password>

# Redis
REDIS_PASS=<generate-strong-password>

# MinIO
MINIO_ACCESS_KEY=jirama
MINIO_SECRET_KEY=<generate-strong-password>

# Monitoring
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASS=<generate-strong-password>

# Deployment
SPRING_PROFILE=prod
```
