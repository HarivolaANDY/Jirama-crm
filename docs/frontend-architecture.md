# Frontend Architecture — React + TypeScript + Vite

## Directory Structure

```
frontend/
├── public/
│   ├── favicon.ico
│   ├── logo.svg
│   ├── og-image.png
│   └── manifests/
│       └── site.webmanifest
│
├── src/
│   ├── main.tsx                      # Entry point
│   ├── App.tsx                       # Root component with providers
│   ├── index.css                     # Global styles + Tailwind base
│   ├── vite-env.d.ts
│   │
│   ├── components/                   # SHARED UI COMPONENTS
│   │   ├── ui/                       # Shadcn UI primitives
│   │   │   ├── button.tsx
│   │   │   ├── card.tsx
│   │   │   ├── dialog.tsx
│   │   │   ├── dropdown-menu.tsx
│   │   │   ├── form.tsx
│   │   │   ├── input.tsx
│   │   │   ├── select.tsx
│   │   │   ├── table.tsx
│   │   │   ├── tabs.tsx
│   │   │   ├── toast.tsx
│   │   │   ├── badge.tsx
│   │   │   ├── avatar.tsx
│   │   │   ├── skeleton.tsx
│   │   │   ├── sheet.tsx
│   │   │   ├── tooltip.tsx
│   │   │   └── ...
│   │   ├── layout/
│   │   │   ├── AppLayout.tsx                # Main authenticated layout
│   │   │   ├── PublicLayout.tsx             # Landing page layout
│   │   │   ├── Sidebar.tsx                  # Navigation sidebar
│   │   │   ├── Topbar.tsx                   # Top navigation bar
│   │   │   ├── Footer.tsx                   # Public footer
│   │   │   └── MobileNav.tsx               # Mobile bottom navigation
│   │   └── shared/
│   │       ├── LoadingSpinner.tsx
│   │       ├── ErrorBoundary.tsx
│   │       ├── EmptyState.tsx
│   │       ├── ConfirmDialog.tsx
│   │       ├── DataTable.tsx                # Generic table with sorting/pagination
│   │       ├── SearchInput.tsx
│   │       ├── StatusBadge.tsx
│   │       ├── AnimatedCounter.tsx
│   │       ├── PageHeader.tsx
│   │       ├── StatCard.tsx
│   │       ├── FileUpload.tsx
│   │       └── MapView.tsx                  # React Leaflet wrapper
│   │
│   ├── features/                    # FEATURE MODULES
│   │   ├── landing/
│   │   │   ├── pages/
│   │   │   │   ├── HomePage.tsx
│   │   │   │   ├── ServicesPage.tsx
│   │   │   │   ├── ContactPage.tsx
│   │   │   │   └── AboutPage.tsx
│   │   │   ├── components/
│   │   │   │   ├── HeroSection.tsx
│   │   │   │   ├── FeaturesSection.tsx
│   │   │   │   ├── StatsSection.tsx
│   │   │   │   ├── TestimonialsSection.tsx
│   │   │   │   └── CTASection.tsx
│   │   │   └── index.ts
│   │   │
│   │   ├── auth/
│   │   │   ├── pages/
│   │   │   │   ├── LoginPage.tsx
│   │   │   │   ├── RegisterPage.tsx
│   │   │   │   ├── ForgotPasswordPage.tsx
│   │   │   │   └── ResetPasswordPage.tsx
│   │   │   ├── components/
│   │   │   │   ├── LoginForm.tsx
│   │   │   │   ├── RegisterForm.tsx
│   │   │   │   ├── OAuthCallback.tsx
│   │   │   │   └── ProtectedRoute.tsx
│   │   │   └── hooks/
│   │   │       └── useAuth.ts
│   │   │
│   │   ├── customer/                # CUSTOMER PORTAL
│   │   │   ├── pages/
│   │   │   │   ├── DashboardPage.tsx
│   │   │   │   ├── BillsPage.tsx
│   │   │   │   ├── BillDetailPage.tsx
│   │   │   │   ├── PaymentsPage.tsx
│   │   │   │   ├── MakePaymentPage.tsx
│   │   │   │   ├── ConsumptionPage.tsx
│   │   │   │   ├── ConsumptionGraphPage.tsx
│   │   │   │   ├── ReportIncidentPage.tsx
│   │   │   │   ├── IncidentsPage.tsx
│   │   │   │   ├── ComplaintsPage.tsx
│   │   │   │   ├── FileComplaintPage.tsx
│   │   │   │   ├── AgenciesPage.tsx
│   │   │   │   ├── NotificationsPage.tsx
│   │   │   │   ├── ProfilePage.tsx
│   │   │   │   ├── ContractsPage.tsx
│   │   │   │   ├── ConnectionRequestPage.tsx
│   │   │   │   └── ReferralPage.tsx
│   │   │   ├── components/
│   │   │   │   ├── BillCard.tsx
│   │   │   │   ├── BillList.tsx
│   │   │   │   ├── PaymentForm.tsx
│   │   │   │   ├── PaymentHistoryTable.tsx
│   │   │   │   ├── ConsumptionChart.tsx
│   │   │   │   ├── ConsumptionComparison.tsx
│   │   │   │   ├── IncidentForm.tsx
│   │   │   │   ├── IncidentCard.tsx
│   │   │   │   ├── ComplaintForm.tsx
│   │   │   │   ├── ComplaintTracker.tsx
│   │   │   │   ├── AgencyMap.tsx
│   │   │   │   ├── QrScanner.tsx
│   │   │   │   ├── ContractCard.tsx
│   │   │   │   ├── ProfileForm.tsx
│   │   │   │   └── ReferralProgram.tsx
│   │   │   └── hooks/
│   │   │       ├── useBills.ts
│   │   │       ├── usePayments.ts
│   │   │       ├── useConsumption.ts
│   │   │       ├── useIncidents.ts
│   │   │       └── useContracts.ts
│   │   │
│   │   ├── agent/                   # AGENT CRM
│   │   │   ├── pages/
│   │   │   │   ├── AgentDashboardPage.tsx
│   │   │   │   ├── SubscribersPage.tsx
│   │   │   │   ├── SubscriberDetailPage.tsx
│   │   │   │   ├── ContractsPage.tsx
│   │   │   │   ├── MetersPage.tsx
│   │   │   │   ├── MeterDetailPage.tsx
│   │   │   │   ├── BillingPage.tsx
│   │   │   │   ├── PaymentsPage.tsx
│   │   │   │   ├── IncidentsPage.tsx
│   │   │   │   ├── IncidentDetailPage.tsx
│   │   │   │   ├── ComplaintsPage.tsx
│   │   │   │   └── ValidationPage.tsx
│   │   │   └── components/
│   │   │       ├── SubscriberTable.tsx
│   │   │       ├── SubscriberForm.tsx
│   │   │       ├── ContractForm.tsx
│   │   │       ├── MeterForm.tsx
│   │   │       ├── InvoiceTable.tsx
│   │   │       ├── PaymentTable.tsx
│   │   │       ├── IncidentTable.tsx
│   │   │       ├── ValidationWorkflow.tsx
│   │   │       └── ActionLogTimeline.tsx
│   │   │
│   │   ├── technician/              # TECHNICIAN CRM
│   │   │   ├── pages/
│   │   │   │   ├── TechDashboardPage.tsx
│   │   │   │   ├── MyRoutesPage.tsx
│   │   │   │   ├── OutagesPage.tsx
│   │   │   │   ├── WorkOrdersPage.tsx
│   │   │   │   └── MeterReadingPage.tsx
│   │   │   └── components/
│   │   │       ├── RouteMap.tsx
│   │   │       ├── WorkOrderCard.tsx
│   │   │       ├── MeterReadingForm.tsx
│   │   │       └── OutageStatusBadge.tsx
│   │   │
│   │   ├── admin/                   # ADMINISTRATOR CRM
│   │   │   ├── pages/
│   │   │   │   ├── AdminDashboardPage.tsx
│   │   │   │   ├── UsersPage.tsx
│   │   │   │   ├── RolesPage.tsx
│   │   │   │   ├── AuditLogsPage.tsx
│   │   │   │   ├── SystemConfigPage.tsx
│   │   │   │   ├── RegionsPage.tsx
│   │   │   │   └── AgentsPage.tsx
│   │   │   └── components/
│   │   │       ├── UserTable.tsx
│   │   │       ├── UserForm.tsx
│   │   │       ├── AuditLogTable.tsx
│   │   │       ├── SystemConfigForm.tsx
│   │   │       └── RegionMap.tsx
│   │   │
│   │   ├── dashboard/               # MANAGEMENT DASHBOARD
│   │   │   ├── pages/
│   │   │   │   └── ManagementDashboardPage.tsx
│   │   │   └── components/
│   │   │       ├── KpiCard.tsx
│   │   │       ├── RevenueChart.tsx
│   │   │       ├── RegionalStats.tsx
│   │   │       ├── IncidentTrends.tsx
│   │   │       ├── PaymentSuccessRate.tsx
│   │   │       ├── TopConsumers.tsx
│   │   │       └── CollectionRate.tsx
│   │   │
│   │   ├── notifications/
│   │   │   ├── pages/
│   │   │   │   ├── NotificationCenterPage.tsx
│   │   │   │   └── NotificationTemplatesPage.tsx
│   │   │   ├── components/
│   │   │   │   ├── NotificationList.tsx
│   │   │   │   ├── NotificationBell.tsx
│   │   │   │   └── TemplateForm.tsx
│   │   │   └── hooks/
│   │   │       └── useNotifications.ts
│   │   │
│   │   ├── support/
│   │   │   ├── pages/
│   │   │   │   ├── SupportCenterPage.tsx
│   │   │   │   └── LiveChatPage.tsx
│   │   │   └── components/
│   │   │       ├── TicketForm.tsx
│   │   │       ├── TicketList.tsx
│   │   │       ├── ChatWindow.tsx
│   │   │       └── FaqAccordion.tsx
│   │   │
│   │   ├── documents/
│   │   │   ├── pages/
│   │   │   │   ├── DocumentListPage.tsx
│   │   │   │   └── DocumentUploadPage.tsx
│   │   │   └── components/
│   │   │       ├── DocumentGrid.tsx
│   │   │       ├── DocumentPreview.tsx
│   │   │       └── UploadDropzone.tsx
│   │   │
│   │   └── reports/
│   │       ├── pages/
│   │       │   ├── ReportListPage.tsx
│   │       │   └── ReportBuilderPage.tsx
│   │       └── components/
│   │           ├── ReportCard.tsx
│   │           ├── ReportChart.tsx
│   │           └── ExportOptions.tsx
│   │
│   ├── hooks/                       # SHARED HOOKS
│   │   ├── useMediaQuery.ts
│   │   ├── useDebounce.ts
│   │   ├── useLocalStorage.ts
│   │   ├── usePagination.ts
│   │   └── usePermissions.ts
│   │
│   ├── lib/                         # UTILITIES
│   │   ├── utils.ts                 # cn() helper, etc.
│   │   ├── constants.ts             # App constants
│   │   ├── formatters.ts            # Date, currency, number formatters
│   │   └── validators.ts            # Zod schemas for forms
│   │
│   ├── providers/
│   │   ├── AuthProvider.tsx          # Keycloak auth context
│   │   ├── ThemeProvider.tsx         # Light/Dark mode
│   │   └── QueryProvider.tsx         # TanStack Query provider
│   │
│   ├── routes/
│   │   ├── index.tsx                 # Route tree definition
│   │   ├── public.routes.tsx         # Public (unauthenticated) routes
│   │   ├── protected.routes.tsx      # Authenticated routes (with role guards)
│   │   └── role-guards.tsx           # Role-based route protection
│   │
│   ├── types/
│   │   ├── api.ts                    # API response/request types
│   │   ├── models.ts                 # Domain model types
│   │   ├── forms.ts                  # Form types
│   │   └── index.ts
│   │
│   └── api/                         # API CLIENT
│       ├── client.ts                 # Axios instance with interceptors
│       ├── subscribers.api.ts
│       ├── invoices.api.ts
│       ├── payments.api.ts
│       ├── incidents.api.ts
│       ├── complaints.api.ts
│       ├── notifications.api.ts
│       ├── documents.api.ts
│       ├── users.api.ts
│       ├── dashboard.api.ts
│       └── reports.api.ts
│
├── vitest.config.ts                  # Vitest configuration
├── tailwind.config.ts                # Tailwind theme (colors, fonts)
├── tsconfig.json                     # Strict TypeScript config
├── vite.config.ts                    # Vite config with proxy
└── package.json
```

## Route Structure

```
/                                    → Landing Page (PublicLayout)
├── login                            → Login (PublicLayout)
├── register                         → Register (PublicLayout)
├── forgot-password                   → Forgot Password (PublicLayout)
│
├── customer/                        → Customer Portal (AppLayout)
│   ├── dashboard                    → Customer Dashboard
│   ├── bills                        → Bills List
│   ├── bills/:id                    → Bill Detail + Pay
│   ├── payments                     → Payment History
│   ├── consumption                  → Consumption + Graphs
│   ├── incidents                    → My Incidents
│   ├── incidents/new                → Report Incident
│   ├── complaints                   → My Complaints
│   ├── complaints/new               → File Complaint
│   ├── agencies                     → Find Agencies (Map)
│   ├── profile                      → Profile Settings
│   ├── contracts                    → My Contracts
│   ├── notifications                → Notifications
│   ├── referrals                    → Referral Program
│   └── support                      → Support / Chat
│
├── agent/                           → Agent CRM (AppLayout)
│   ├── dashboard                    → Agent Dashboard
│   ├── subscribers                  → Manage Subscribers
│   ├── subscribers/:id              → Subscriber Detail
│   ├── contracts                    → Manage Contracts
│   ├── meters                       → Manage Meters
│   ├── billing                      → Billing Management
│   ├── payments                     → Payment Management
│   ├── incidents                    → Manage Incidents
│   ├── complaints                   → Manage Complaints
│   └── validation                   → Validation Workflow
│
├── technician/                      → Technician CRM (AppLayout)
│   ├── dashboard                    → Tech Dashboard
│   ├── routes                       → My Routes
│   ├── outages                      → Outage Management
│   ├── work-orders                  → Work Orders
│   └── readings                     → Meter Readings
│
├── admin/                           → Admin Panel (AppLayout)
│   ├── dashboard                    → Admin Dashboard
│   ├── users                        → User Management
│   ├── roles                        → Role Management
│   ├── audit-logs                   → Audit Logs
│   ├── configuration                → System Config
│   └── regions                      → Regions Management
│
├── management/                      → Management Dashboard (AppLayout)
│   └── dashboard                    → KPI Dashboard
│
├── reports/                         → Reports (AppLayout)
│   ├── list                         → Report List
│   └── builder                      → Report Builder
│
├── notifications                    → Notification Center (AppLayout)
└── documents                        → Document Management (AppLayout)
```

## State Management Strategy

| State Type | Tool | Example |
|---|---|---|
| **Server State** | TanStack Query (React Query) | Bills, payments, subscribers, incidents |
| **Client State** | Zustand | Sidebar state, theme, filters, modals |
| **Auth State** | React Context + Keycloak | Current user, roles, tokens |
| **Form State** | React Hook Form | All form inputs |
| **URL State** | React Router | Search params, filters, pagination |

## Animation Strategy (Framer Motion)

| Component | Animation | Trigger |
|---|---|---|
| Page transitions | `fadeIn` + `slideIn` | Route change |
| Stat counters | `AnimatedCounter` | Intersection Observer |
| Cards | `hover: scale(1.02)` + shadow | Mouse hover |
| Lists | `staggerChildren` | Mount |
| Skeleton loading | Pulse animation | Loading state |
| Notifications | `slideIn` from top | New notification |
| Dialogs/Sheets | `scale` + `opacity` | Open/close |
| Charts | `animatePresence` | Data change |

All animations are:
- **Hardware-accelerated** (transform + opacity only)
- **Respect `prefers-reduced-motion`**
- **Gated by `disableAnimations` setting** for low-end devices

## i18n Strategy

- **Library:** react-i18next
- **Languages:** French (primary), Malagasy (secondary)
- **Storage:** JSON flat files per locale
- **Key pattern:** `module.section.key` (e.g., `customer.bills.pay_now`)
- **Lazy loading:** Namespace per module

## Code Quality Enforcement

```json
{
  "compilerOptions": {
    "strict": true,
    "noUncheckedIndexedAccess": true,
    "noImplicitReturns": true,
    "noFallthroughCasesInSwitch": true,
    "exactOptionalPropertyTypes": true,
    "forceConsistentCasingInFileNames": true
  }
}
```

- ESLint with `@typescript-eslint/strict`
- Prettier for formatting
- Husky + lint-staged for pre-commit checks
- Vitest + Testing Library for unit tests
- Playwright for e2e tests
