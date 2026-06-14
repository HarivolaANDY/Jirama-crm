# Phased Development Plan

## Overview

The JIRAMA CRM platform is a large-scale enterprise application. We recommend a phased approach over 6-8 months with 5 major phases, each delivering a working, deployable increment.

---

## Phase 1: Foundation & Authentication (Weeks 1-4)

### Goal
Establish the project foundation, infrastructure, and authentication so all subsequent phases build on a solid base.

### Backend Tasks
- [ ] Initialize Spring Boot project with Clean Architecture package structure
- [ ] Configure Spring Security + Keycloak OAuth2 integration
- [ ] Implement PostgreSQL connection with Flyway migrations
- [ ] Create database schema: `regions`, `users`, `audit_logs`
- [ ] Implement users CRUD with RBAC
- [ ] Configure Redis for token caching
- [ ] Set up global exception handling
- [ ] Add Swagger/OpenAPI documentation
- [ ] Write integration tests for auth flow

### Frontend Tasks
- [ ] Initialize React + Vite + TypeScript project
- [ ] Configure Tailwind CSS with JIRAMA theme (colors, fonts)
- [ ] Integrate Shadcn UI components
- [ ] Set up project directory structure (features, components, hooks, lib)
- [ ] Configure Keycloak JS adapter with PKCE
- [ ] Implement AuthProvider and ProtectedRoute
- [ ] Create AppLayout, Sidebar, Topbar components
- [ ] Implement Login and Register pages
- [ ] Set up React Query + Zustand
- [ ] Configure i18n (FR/MG)
- [ ] Set up Framer Motion with fade/slide transitions
- [ ] Write unit tests for auth flow

### Infrastructure
- [ ] Create Docker Compose for all services
- [ ] Configure Nginx reverse proxy
- [ ] Set up GitHub Actions CI pipeline
- [ ] Create Keycloak realm export with roles

### Deliverables
- ✅ Running infrastructure (PostgreSQL, Redis, Keycloak, MinIO)
- ✅ Login/Register working with Keycloak
- ✅ Role-based routing (Customer, Agent, Technician, Admin, Manager)
- ✅ CI pipeline passing
- ✅ Project documentation

### Effort: 2 backend devs + 2 frontend devs + 1 DevOps

---

## Phase 2: Customer Portal Core (Weeks 5-9)

### Goal
Deliver the complete Customer Portal with bill viewing, payments, consumption tracking, and incident reporting.

### Backend Tasks
- [ ] Database schema: `subscribers`, `contracts`, `meters`, `consumption_readings`
- [ ] Subscriber CRUD with search
- [ ] Contract and Meter management
- [ ] Consumption reading submission
- [ ] Invoice generation engine (batch)
- [ ] Payment processing (cash/card online)
- [ ] PDF invoice generation (MinIO storage)
- [ ] Mobile Money payment integration (MVola, Orange Money APIs)
- [ ] Incident reporting and tracking
- [ ] Complaint management with message threads
- [ ] Notification system (in-app)
- [ ] Write unit + integration tests for all new endpoints

### Frontend Tasks
- [ ] Customer Dashboard page with KPIs
- [ ] Bills list with filtering and search
- [ ] Bill detail page with PDF download
- [ ] Payment form (card + Mobile Money)
- [ ] Payment history table
- [ ] Consumption visualization (Recharts)
- [ ] Consumption comparison month-over-month
- [ ] Report Incident form with map location picker (Leaflet)
- [ ] My Incidents list with status tracking
- [ ] File Complaint form with attachments
- [ ] Complaint tracker with message thread
- [ ] Multi-contract management page
- [ ] QR invoice scanning (libraries: `html5-qrcode`)

### Customer Portal Pages Completed
- Dashboard, Bills, Bill Detail, Payments, Consumption, Consumption Graphs
- Incidents (list, report), Complaints (list, file, track)
- Profile, Contracts, Notifications
- Agency Map (geolocation)

### Effort: 2 backend + 2 frontend + 1 integration specialist (payments)

---

## Phase 3: Agent & Technician CRM (Weeks 10-14)

### Goal
Deliver the full Agent CRM and Technician CRM modules.

### Backend Tasks
- [ ] Agent dashboard data aggregation
- [ ] Advanced subscriber search (full-text, multi-criteria)
- [ ] Subscriber contract and meter assignment workflow
- [ ] Invoice management (generate, cancel, reissue)
- [ ] Payment processing (agent-side: cash)
- [ ] Incident management with team assignment
- [ ] Validation workflow engine (documents → inspection → approval)
- [ ] Outage management (planned & unplanned)
- [ ] Work order system for technicians
- [ ] Technician route management
- [ ] Meter reading submission with work order linking
- [ ] Call center complaint handling
- [ ] Action logging and activity timeline
- [ ] Write tests

### Frontend Tasks
- [ ] Agent Dashboard with workload summary
- [ ] Subscriber management table with advanced search
- [ ] Subscriber detail with contracts, meters, invoices timeline
- [ ] Contract creation/modification form
- [ ] Meter registration and status management
- [ ] Invoice batch generation UI
- [ ] Incident management table with filters
- [ ] Validation workflow UI (document review → approve/reject)
- [ ] Action log timeline component
- [ ] Technician Dashboard with route overview
- [ ] Route map visualization (Leaflet)
- [ ] Work order cards with status updates
- [ ] Meter reading form (offline-capable)
- [ ] Outage management with calendar/schedule

### Agent CRM Pages Completed
- Dashboard, Subscribers, Subscriber Detail, Contracts, Meters
- Billing, Payments, Incidents, Complaints, Validation Workflow

### Technician CRM Pages Completed
- Dashboard, My Routes, Outages, Work Orders, Meter Readings

### Effort: 2 backend + 2 frontend + 1 designer

---

## Phase 4: Admin & Management Dashboards (Weeks 15-17)

### Goal
Deliver the Admin Panel, Management Dashboard, Reporting, and Notification Center.

### Backend Tasks
- [ ] Admin user management (CRUD, role assignment)
- [ ] Audit log viewer with filters
- [ ] System configuration management
- [ ] Region and agency management
- [ ] Dashboard KPI aggregation queries
- [ ] Revenue and collection statistics
- [ ] Regional statistics engine
- [ ] Report generation service (PDF, Excel, CSV)
- [ ] Scheduled report execution
- [ ] Mass notification service (email, SMS)
- [ ] Notification template management
- [ ] Document management (upload, version, archive)

### Frontend Tasks
- [ ] Admin Dashboard with system health
- [ ] User management table with role management
- [ ] Audit log viewer with search and filters
- [ ] System configuration form
- [ ] Region management with map
- [ ] Management Dashboard with KPI cards
- [ ] Revenue chart (daily/weekly/monthly)
- [ ] Collection rate gauge
- [ ] Incident trends chart
- [ ] Top consumers table
- [ ] Payment success rate visualization
- [ ] Report list and builder
- [ ] Export options (PDF, Excel)
- [ ] Mass notification composer
- [ ] Notification center with templates
- [ ] Document grid with preview and upload

### Admin Pages Completed
- Dashboard, Users, Roles, Audit Logs, Configuration, Regions

### Management Pages Completed
- KPI Dashboard with all charts

### Other Pages Completed
- Reports, Notification Center, Document Management

### Effort: 2 backend + 2 frontend

---

## Phase 5: Support, Referrals & Polish (Weeks 18-20)

### Goal
Deliver remaining features: Live Chat, Support Center, Referral Program, and final polish.

### Backend Tasks
- [ ] Live chat support (WebSocket)
- [ ] FAQ management
- [ ] Referral program engine
- [ ] Connection request tracking with workflow
- [ ] Subscriber export feature
- [ ] Performance optimization (index analysis, query tuning)
- [ ] Cache strategy implementation (Redis)
- [ ] Load testing and optimization

### Frontend Tasks
- [ ] Live Chat window (WebSocket-based)
- [ ] Support Center with FAQ accordion
- [ ] Ticket system
- [ ] Referral program page with stats
- [ ] Referral leaderboard
- [ ] Connection request submission and tracking
- [ ] Mobile responsiveness audit and fixes
- [ ] Accessibility audit (a11y)
- [ ] Performance audit (Lighthouse)
- [ ] Loading states and skeleton screens
- [ ] Error states and empty states
- [ ] Offline capabilities for technicians (PWA)
- [ ] Final UI polish and animation refinement

### Support Pages Completed
- Support Center, Live Chat, FAQ

### Referral Pages Completed
- Referral Program, Leaderboard

### Final Deliverables
- ✅ Complete Customer Portal (24 modules)
- ✅ Complete Agent CRM (20 modules)
- ✅ Complete Technician CRM
- ✅ Complete Admin CRM
- ✅ Management Dashboard
- ✅ Notification Center
- ✅ Support Center
- ✅ Document Management
- ✅ Reporting & Analytics
- ✅ Live Chat
- ✅ Referral Program

### Effort: 1 backend + 1 frontend + 1 QA

---

## Total Timeline: ~20 weeks (5 months)

| Phase | Duration | Total Effort |
|---|---|---|
| Phase 1: Foundation | 4 weeks | 5 FTE |
| Phase 2: Customer Portal | 5 weeks | 5 FTE |
| Phase 3: Agent & Technician CRM | 5 weeks | 5 FTE |
| Phase 4: Admin & Dashboards | 3 weeks | 4 FTE |
| Phase 5: Support & Polish | 3 weeks | 3 FTE |
| **Total** | **20 weeks** | |

## Recommended Team

| Role | Qty |
|---|---|
| Senior Java/Spring Boot Developer | 2 |
| Senior React/TypeScript Developer | 2 |
| UI/UX Designer | 1 |
| DevOps Engineer | 1 (shared) |
| QA Engineer | 1 |
| Technical Project Manager | 1 |
| **Total** | **8** |

## Risk Mitigation

| Risk | Impact | Mitigation |
|---|---|---|
| Keycloak complexity | Medium | Use Keycloak starter + realm export from day 1 |
| Payment integration delays | High | Start Mobile Money integration early (Phase 2) |
| Performance at scale | Medium | Implement caching strategy from Phase 1, load test Phase 5 |
| Mobile Money API availability | High | Have fallback payment methods (bank, card) |
| Data migration from legacy | High | Plan migration strategy in Phase 1, involve domain experts |
| Requirements changes | Medium | Use 2-week sprints with demos; ADRs for architecture decisions |
