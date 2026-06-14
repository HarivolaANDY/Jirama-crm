# System Architecture

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Users                               │
│  [Citizen] [Agent] [Technician] [Admin] [Management]       │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTPS
┌────────────────────▼────────────────────────────────────────┐
│                      Nginx (Reverse Proxy)                   │
│           /api/* → Backend | /* → Static SPA                │
└─────────┬──────────────────┬───────────────────────────────┘
          │                  │
┌─────────▼──────────┐ ┌────▼──────────────────────────┐
│   Keycloak (IAM)    │ │   Frontend (React SPA)        │
│   • Authentication  │ │   • Vite Bundle               │
│   • SSO             │ │   • Shadcn UI Components      │
│   • User Federation │ │   • i18n (FR/MG)              │
│   • RBAC (Roles)    │ │   • Framer Motion Animations  │
└─────────────────────┘ └─────────┬────────────────────┘
                                  │ REST API (JWT Bearer)
                         ┌───────▼────────────────────────┐
                         │    Spring Boot Backend          │
                         │    ┌─────────────────────────┐ │
                         │    │   REST Controllers       │ │
                         │    ├─────────────────────────┤ │
                         │    │   Application Services   │ │
                         │    ├─────────────────────────┤ │
                         │    │   Domain (Entities)      │ │
                         │    ├─────────────────────────┤ │
                         │    │   Repository Adapters    │ │
                         │    └─────────────────────────┘ │
                         └───────┬────────────────────────┘
          ┌───────────────────────┼──────────┬────────────────┐
          │                       │          │                │
┌─────────▼────────┐  ┌──────────▼──┐ ┌─────▼────┐  ┌──────▼─────┐
│   PostgreSQL 16   │  │   Redis 7   │ │  MinIO   │  │  Prometheus │
│   (Primary DB)    │  │  (Cache/    │ │ (Docs)   │  │ (Metrics)  │
│                   │  │   Sessions) │ │          │  │            │
└───────────────────┘  └─────────────┘ └──────────┘  └──────┬─────┘
                                                            │
                                                     ┌──────▼─────┐
                                                     │   Grafana   │
                                                     │ (Dashboards)│
                                                     └────────────┘
```

## Architecture Principles

### Clean Architecture (Hexagonal)

The backend follows Clean Architecture with strict dependency inversion:

```
     ┌──────────────────────────────────────────┐
     │         Interface Adapters                │
     │  [Controllers] [DTOs] [Mappers]           │
     └────────────┬──────────────────────────────┘
                  │ Dependency Direction (points INWARD)
     ┌────────────▼──────────────────────────────┐
     │         Application Layer                  │
     │  [Use Cases] [Ports (Interfaces)]          │
     │  [DTOs] [Validators]                      │
     └────────────┬──────────────────────────────┘
                  │
     ┌────────────▼──────────────────────────────┐
     │         Domain Layer                       │
     │  [Entities] [Value Objects]                │
     │  [Domain Services] [Domain Events]         │
     │  [Repository Interfaces]                   │
     └────────────────────────────────────────────┘
```

**Key Rules:**
- Domain layer has ZERO framework dependencies (no Spring annotations)
- Application layer depends only on Domain
- Infrastructure adapters implement ports defined in Domain/Application
- Controllers are thin — they delegate to Application Services

### Frontend Component Architecture

```
┌──────────────────────────────────────────────────┐
│                    Layout                         │
│  ┌────────────────────────────────────────────┐  │
│  │            Navigation (Top/Side)           │  │
│  ├────────────────────────────────────────────┤  │
│  │                                            │  │
│  │            <Outlet /> (Pages)              │  │
│  │                                            │  │
│  ├────────────────────────────────────────────┤  │
│  │   Shared Components (Bottom Navigation)    │  │
│  └────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────┘
```

**Component Hierarchy:**
```
pages/           → Route-level components (one per route)
features/        → Feature modules with internal components
components/      → Shared UI primitives (Shadcn-based)
layouts/         → Layout wrappers
providers/       → Context providers (Auth, Theme, i18n)
```

### Data Flow

```
User Action → React Component → React Query Mutation → API Client
    ↓                                                    ↓
UI Optimistic Update ← Query Cache Update ← Spring Controller
                                               ↓
                                         Application Service
                                               ↓
                                         Domain Entity (Logic)
                                               ↓
                                         Repository (JPA/Postgres)
```

### Authentication Flow

```
┌─────────┐          ┌──────────┐          ┌───────────┐
│  Browser │          │ Keycloak │          │  Backend  │
└────┬─────┘          └────┬─────┘          └─────┬─────┘
     │                     │                      │
     │───Login Request────►│                      │
     │                     │                      │
     │◄──Auth Code (PKCE)─┤                      │
     │                     │                      │
     │───Token Exchange───►│                      │
     │◄──JWT (Access+Refresh)─                   │
     │                     │                      │
     │───API Call+JWT────────────────────────────►│
     │                     │                      │
     │                     │───JWKs Validation───►│
     │                     │◄──Public Key────────┤
     │                     │                      │
     │◄──Protected Resource──────────────────────│
```

## Module Responsibilities

| Module | Responsibility |
|---|---|
| **Landing Page** | Public site, company info, login/register entry points |
| **Customer Portal** | Bills, payments, consumption, incidents, profile |
| **Agent CRM** | Subscriber/meter/contract management, incident logging |
| **Technician CRM** | Field routes, outage management, work orders |
| **Admin CRM** | User management, system config, audit logs |
| **Dashboard** | KPIs, regional stats, charts, real-time metrics |
| **Notifications** | Email/SMS/Push notification management |
| **Support Center** | Ticket system, live chat, FAQ |
| **Documents** | Upload, store, retrieve (MinIO), versioning |
| **Reports** | Report generation, export (PDF/Excel), scheduling |

## Technology Decisions

### Why React Query over Redux?
- Server state management is the primary concern
- Built-in caching, pagination, optimistic updates
- Less boilerplate than Redux Toolkit

### Why Zustand for client state?
- Minimalist, TypeScript-first
- Perfect for UI state (modals, toasts, sidebar state)
- No providers needed

### Why Flyway over Liquibase?
- Simpler, SQL-first migrations
- Native Spring Boot integration
- Preferred for PostgreSQL-centric projects

### Why Keycloak over Auth0/Firebase Auth?
- Self-hosted (data sovereignty in Madagascar)
- Full RBAC + fine-grained permissions
- No per-user pricing at scale
- SSO capability for future internal tools
