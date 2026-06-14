# JIRAMA CRM Web Platform + Customer Portal

**JIRAMA** (Jiro sy Rano Malagasy) — Madagascar's national electricity and water utility — modern CRM and customer self-service platform.

## Overview

A comprehensive enterprise web platform enabling:

- **Citizens** to view/pay bills, track consumption, report incidents, and communicate with JIRAMA
- **JIRAMA Agents** to manage subscribers, contracts, meters, payments, incidents, and complaints
- **Technicians** to manage field operations, routes, and outage resolution
- **Administrators** to configure the system, manage users, and view analytics
- **Management** to access dashboards, reports, and regional statistics

## Tech Stack

### Frontend
| Technology | Purpose |
|---|---|
| React 19 | UI Framework |
| TypeScript 5 (Strict Mode) | Type Safety |
| Vite 6 | Build Tool |
| Tailwind CSS 4 | Utility-First Styling |
| Shadcn UI | Component Library |
| Framer Motion | Animations |
| Lucide React | Icons |
| React Router 7 | Routing |
| React Query (TanStack Query) | Server State |
| Zustand | Client State |
| React Hook Form + Zod | Forms & Validation |
| Recharts | Charts & Graphs |
| React Leaflet | Maps (Agency Geolocation) |
| i18next | Internationalization |

### Backend
| Technology | Purpose |
|---|---|
| Spring Boot 3.x | Application Framework |
| Java 21 LTS | Runtime |
| Spring Data JPA | ORM / Persistence |
| Spring Security + OAuth2 | Authentication |
| Spring Doc OpenAPI | API Documentation |
| PostgreSQL 16 | Relational Database |
| Redis 7 | Caching / Sessions |
| MinIO | Object Storage (Documents) |
| Flyway | Database Migrations |

### Infrastructure
| Technology | Purpose |
|---|---|
| Docker & Compose | Containerization |
| Nginx | Reverse Proxy / Static Serving |
| Keycloak | Identity & Access Management |
| Grafana | Monitoring Dashboards |
| Prometheus | Metrics Collection |
| GitHub Actions | CI/CD |

## Project Structure

```
jirama-crm/
├── frontend/                    # React + Vite SPA
│   ├── src/
│   │   ├── components/          # Shared UI components
│   │   ├── features/            # Feature modules
│   │   │   ├── landing/         # Public landing page
│   │   │   ├── auth/            # Authentication
│   │   │   ├── customer/        # Customer portal
│   │   │   ├── agent/           # Agent CRM
│   │   │   ├── technician/      # Technician CRM
│   │   │   ├── admin/           # Admin panel
│   │   │   ├── dashboard/       # Management dashboard
│   │   │   ├── notifications/   # Notification center
│   │   │   ├── support/         # Support center
│   │   │   ├── documents/       # Document management
│   │   │   └── reports/         # Reporting & analytics
│   │   ├── hooks/               # Shared hooks
│   │   ├── lib/                 # Utilities
│   │   ├── providers/           # React context providers
│   │   ├── routes/              # Route definitions
│   │   ├── types/               # TypeScript types
│   │   └── api/                 # API client
│   ├── public/
│   └── package.json
│
├── backend/                     # Spring Boot API
│   ├── src/main/java/com/jirama/
│   │   ├── domain/              # Domain layer (entities, value objects)
│   │   ├── application/         # Use cases, ports
│   │   ├── infrastructure/      # Adapters, persistence, security
│   │   ├── interfaces/          # REST controllers, DTOs
│   │   └── shared/             # Shared kernel
│   ├── src/main/resources/
│   │   ├── db/migration/        # Flyway migrations
│   │   └── application.yml
│   └── pom.xml
│
├── docker/                      # Docker configurations
│   ├── nginx/
│   ├── keycloak/
│   └── monitoring/
│
├── docs/                        # Documentation
│   ├── architecture.md
│   ├── data-model.md
│   ├── api-design.md
│   ├── security.md
│   ├── deployment.md
│   ├── development-plan.md
│   └── diagrams/
│
├── .github/workflows/           # CI/CD pipelines
├── docker-compose.yml
└── README.md
```

## Color Palette

| Role | Color | Hex |
|---|---|---|
| Primary (JIRAMA Blue) | 🟦 | `#0057B8` |
| Accent (Energy Orange) | 🟧 | `#F59E0B` |
| Success | 🟩 | `#22C55E` |
| Error | 🟥 | `#DC2626` |
| Light Background | ⬜ | `#F8FAFC` |
| Dark Background | ⬛ | `#0F172A` |
| Text | ⬛ | `#1E293B` |

## Getting Started

```bash
# Clone the repository
git clone <repository-url> jirama-crm
cd jirama-crm

# Start the infrastructure
docker-compose up -d postgres keycloak redis minio

# Start the backend
cd backend
./mvnw spring-boot:run

# Start the frontend (in another terminal)
cd frontend
pnpm install
pnpm dev
```

## License

Proprietary — JIRAMA
