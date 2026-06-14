# API Design — RESTful Endpoints

## Base URL

```
Production:  https://api.jirama.mg/api/v1
Staging:     https://staging-api.jirama.mg/api/v1
Local:       http://localhost:8080/api/v1
```

## Authentication

All endpoints (except public ones) require a Bearer JWT token:

```
Authorization: Bearer <access_token>
```

## API Standards

### Pagination

All list endpoints support:

```
GET /api/v1/subscribers?page=0&size=20&sort=createdAt,desc
```

Response:
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "last": false,
  "first": true
}
```

### Error Response

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2026-06-14T10:30:00Z",
  "path": "/api/v1/subscribers",
  "errors": [
    { "field": "email", "message": "must be a valid email address" }
  ]
}
```

### Common Query Parameters

| Parameter | Type | Description |
|---|---|---|
| `page` | int | Zero-based page index (default: 0) |
| `size` | int | Page size (default: 20, max: 100) |
| `sort` | string | Sort field, direction: `field,direction` |
| `q` | string | Search query (full-text search) |
| `status` | string | Filter by status |

---

## Authentication & User Endpoints

### Public Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register new customer account |
| POST | `/auth/login` | Login (returns Keycloak auth URL) |
| POST | `/auth/refresh` | Refresh token |
| POST | `/auth/forgot-password` | Send password reset email |
| POST | `/auth/reset-password` | Reset password with token |
| GET | `/auth/keycloak-config` | Get Keycloak public config (issuer, clientId) |

### User Profile

| Method | Endpoint | Roles |
|---|---|---|
| GET | `/users/me` | All authenticated |
| PUT | `/users/me` | All authenticated |
| PUT | `/users/me/password` | All authenticated |
| GET | `/users` | ADMIN |
| GET | `/users/{id}` | ADMIN, AGENT |
| POST | `/users` | ADMIN |
| PUT | `/users/{id}` | ADMIN |
| DELETE | `/users/{id}` | ADMIN |
| PUT | `/users/{id}/role` | ADMIN |

---

## Subscriber Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| POST | `/subscribers` | AGENT, ADMIN | Create subscriber |
| GET | `/subscribers` | AGENT, ADMIN | List/search subscribers |
| GET | `/subscribers/{id}` | AGENT, ADMIN, CUSTOMER | Get subscriber detail |
| GET | `/subscribers/my` | CUSTOMER | Get own subscriber profile |
| PUT | `/subscribers/{id}` | AGENT, ADMIN | Update subscriber |
| DELETE | `/subscribers/{id}` | ADMIN | Soft-delete subscriber |
| GET | `/subscribers/{id}/contracts` | AGENT, ADMIN, CUSTOMER | Get subscriber's contracts |
| GET | `/subscribers/{id}/invoices` | AGENT, ADMIN, CUSTOMER | Get subscriber's invoices |
| GET | `/subscribers/{id}/payments` | AGENT, ADMIN, CUSTOMER | Get subscriber's payments |
| GET | `/subscribers/{id}/incidents` | AGENT, ADMIN, CUSTOMER | Get subscriber's incidents |
| GET | `/subscribers/search` | AGENT, ADMIN | Advanced search (number, name, phone) |
| GET | `/subscribers/export` | AGENT, ADMIN | Export subscribers (CSV/Excel) |

### Request: Create Subscriber

```json
{
  "firstName": "Jean",
  "lastName": "Rakoto",
  "email": "jean.rakoto@email.com",
  "phoneNumber": "+261 34 12 345 67",
  "idCardNumber": "101 012 345 678",
  "addressLine1": "Lot IVK 123 Ambohimanarina",
  "city": "Antananarivo",
  "regionId": "uuid-of-analamanga",
  "subscriberType": "residential",
  "preferredLanguage": "fr"
}
```

### Response: Subscriber

```json
{
  "id": "uuid",
  "subscriberNumber": "JRM-2026-000001",
  "firstName": "Jean",
  "lastName": "Rakoto",
  "email": "jean.rakoto@email.com",
  "phoneNumber": "+261 34 12 345 67",
  "address": {
    "line1": "Lot IVK 123 Ambohimanarina",
    "city": "Antananarivo",
    "region": "Analamanga"
  },
  "status": "active",
  "subscriberType": "residential",
  "contracts": [
    {
      "id": "uuid",
      "contractNumber": "CTR-2026-000001",
      "contractType": "electricity",
      "status": "active",
      "tariffCode": "T1"
    }
  ],
  "createdAt": "2026-06-14T10:30:00Z"
}
```

---

## Contract Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| POST | `/contracts` | AGENT, ADMIN | Create contract |
| GET | `/contracts` | AGENT, ADMIN | List contracts |
| GET | `/contracts/{id}` | AGENT, ADMIN, CUSTOMER | Get contract detail |
| PUT | `/contracts/{id}` | AGENT, ADMIN | Update contract |
| PATCH | `/contracts/{id}/status` | AGENT, ADMIN | Suspend/terminate contract |
| GET | `/contracts/{id}/meters` | AGENT, ADMIN, CUSTOMER | Get contract's meters |
| GET | `/contracts/{id}/invoices` | AGENT, ADMIN, CUSTOMER | Get contract's invoices |
| GET | `/contracts/{id}/consumption` | AGENT, ADMIN, CUSTOMER | Get contract's consumption history |

---

## Meter Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| POST | `/meters` | AGENT, ADMIN, TECHNICIAN | Create/register meter |
| GET | `/meters` | AGENT, ADMIN, TECHNICIAN | List meters |
| GET | `/meters/{id}` | AGENT, ADMIN, TECHNICIAN, CUSTOMER | Get meter detail |
| PUT | `/meters/{id}` | AGENT, ADMIN, TECHNICIAN | Update meter |
| PATCH | `/meters/{id}/status` | AGENT, ADMIN | Change meter status |
| GET | `/meters/{id}/readings` | AGENT, ADMIN, TECHNICIAN, CUSTOMER | Get meter readings |
| POST | `/meters/{id}/readings` | TECHNICIAN | Submit meter reading |
| GET | `/meters/scan/{qrCode}` | CUSTOMER | Lookup meter by QR code |

---

## Billing / Invoice Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| POST | `/invoices` | ADMIN (batch) | Generate invoices (batch job) |
| GET | `/invoices` | AGENT, ADMIN, CUSTOMER | List invoices (filterable) |
| GET | `/invoices/{id}` | AGENT, ADMIN, CUSTOMER | Get invoice detail |
| GET | `/invoices/{id}/pdf` | AGENT, ADMIN, CUSTOMER | Download invoice PDF |
| GET | `/invoices/my/current` | CUSTOMER | Get current unpaid invoice |
| GET | `/invoices/my` | CUSTOMER | Get own invoices |
| GET | `/invoices/my/unpaid` | CUSTOMER | Get unpaid invoices only |
| GET | `/invoices/overdue` | AGENT, ADMIN | Get all overdue invoices |
| POST | `/invoices/batch-generate` | ADMIN | Trigger batch invoice generation |

---

## Payment Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| POST | `/payments` | ALL | Process payment |
| GET | `/payments` | AGENT, ADMIN | List payments |
| GET | `/payments/{id}` | ALL | Get payment detail |
| GET | `/payments/my` | CUSTOMER | Get own payment history |
| GET | `/payments/receipt/{id}` | ALL | Download payment receipt |
| POST | `/payments/mobile-money` | CUSTOMER | Initiate Mobile Money payment |
| POST | `/payments/mobile-money/callback` | Public | Mobile Money callback/webhook |
| POST | `/payments/bank` | CUSTOMER | Initiate bank transfer |
| GET | `/payments/methods` | ALL | Get available payment methods |

### Request: Process Payment

```json
{
  "invoiceId": "uuid",
  "amount": 45000.00,
  "paymentMethod": "mobile_money",
  "mobileMoneyProvider": "Mvola",
  "phoneNumber": "+261 34 12 345 67"
}
```

---

## Incident Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| POST | `/incidents` | CUSTOMER, AGENT | Report incident |
| GET | `/incidents` | AGENT, ADMIN, TECHNICIAN | List incidents |
| GET | `/incidents/my` | CUSTOMER | Get own incidents |
| GET | `/incidents/{id}` | ALL | Get incident detail |
| PATCH | `/incidents/{id}/status` | AGENT, TECHNICIAN, ADMIN | Update status |
| PATCH | `/incidents/{id}/assign` | ADMIN | Assign to team |
| PUT | `/incidents/{id}` | AGENT, ADMIN | Update incident |
| GET | `/incidents/nearby` | ALL | Get incidents near location |
| GET | `/incidents/stats` | ADMIN, MANAGER | Incident statistics |

### Request: Report Incident

```json
{
  "incidentType": "power_outage",
  "severity": "high",
  "description": "No electricity in Ambohimanarina since 2 hours",
  "locationLat": -18.8792,
  "locationLng": 47.5079,
  "address": "Lot IVK 123 Ambohimanarina",
  "contractId": "uuid"
}
```

---

## Outage Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| POST | `/outages` | ADMIN, AGENT | Create outage notification |
| GET | `/outages` | ALL | List outages |
| GET | `/outages/current` | ALL | Get current active outages |
| GET | `/outages/planned` | ALL | Get planned outages |
| GET | `/outages/{id}` | ALL | Get outage detail |
| PATCH | `/outages/{id}/status` | ADMIN, TECHNICIAN | Update outage status |
| PUT | `/outages/{id}` | ADMIN | Update outage |
| POST | `/outages/{id}/notify` | ADMIN | Notify affected subscribers |

---

## Complaint Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| POST | `/complaints` | CUSTOMER | File a complaint |
| GET | `/complaints` | AGENT, ADMIN | List complaints |
| GET | `/complaints/my` | CUSTOMER | Get own complaints |
| GET | `/complaints/{id}` | ALL | Get complaint detail |
| PATCH | `/complaints/{id}/status` | AGENT, ADMIN | Update status |
| PATCH | `/complaints/{id}/assign` | ADMIN | Assign to agent |
| POST | `/complaints/{id}/messages` | ALL | Add message to thread |
| GET | `/complaints/{id}/messages` | ALL | Get complaint thread |
| POST | `/complaints/{id}/rate` | CUSTOMER | Rate resolution |

---

## Notification Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| GET | `/notifications` | ALL | Get notifications |
| GET | `/notifications/unread-count` | ALL | Get unread count |
| PATCH | `/notifications/{id}/read` | ALL | Mark as read |
| PATCH | `/notifications/read-all` | ALL | Mark all as read |
| POST | `/notifications/test` | ADMIN | Send test notification |
| GET | `/notifications/templates` | ADMIN | List templates |
| POST | `/notifications/templates` | ADMIN | Create template |
| PUT | `/notifications/templates/{id}` | ADMIN | Update template |
| POST | `/notifications/send-mass` | ADMIN | Send mass notification |
| GET | `/notifications/settings` | ALL | Get notification preferences |
| PUT | `/notifications/settings` | ALL | Update notification preferences |

---

## Document Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| POST | `/documents/upload` | ALL | Upload document (multipart) |
| GET | `/documents` | ALL | List documents |
| GET | `/documents/{id}` | ALL | Get document metadata |
| GET | `/documents/{id}/download` | ALL | Download document |
| DELETE | `/documents/{id}` | ADMIN | Archive document |
| GET | `/documents/{entityType}/{entityId}` | ALL | Get documents for entity |

---

## Dashboard & Statistics Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| GET | `/dashboard/kpi` | MANAGER, ADMIN | Key Performance Indicators |
| GET | `/dashboard/revenue` | MANAGER, ADMIN | Revenue stats (by period) |
| GET | `/dashboard/incidents` | MANAGER, ADMIN | Incident trends |
| GET | `/dashboard/regional-stats` | MANAGER, ADMIN | Stats by region |
| GET | `/dashboard/collection-rate` | MANAGER, ADMIN | Payment collection rates |
| GET | `/dashboard/top-consumers` | MANAGER, ADMIN | Top consumers |
| GET | `/dashboard/subscriber-growth` | MANAGER, ADMIN | Subscriber growth trends |

---

## Report Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| POST | `/reports/generate` | MANAGER, ADMIN | Generate report |
| GET | `/reports` | MANAGER, ADMIN | List saved reports |
| GET | `/reports/{id}/download` | MANAGER, ADMIN | Download report (PDF/Excel) |
| DELETE | `/reports/{id}` | MANAGER, ADMIN | Delete report |
| GET | `/reports/scheduled` | ADMIN | List scheduled reports |
| POST | `/reports/scheduled` | ADMIN | Schedule recurring report |

---

## Agent Functionality Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| GET | `/agents/dashboard` | AGENT | Agent-specific dashboard data |
| GET | `/agents/tasks` | AGENT | Get assigned tasks |
| PATCH | `/agents/tasks/{id}/complete` | AGENT | Mark task complete |
| GET | `/agents/validation-queue` | AGENT | Get pending validations |
| PATCH | `/agents/validation/{id}/approve` | AGENT | Approve validation request |
| PATCH | `/agents/validation/{id}/reject` | AGENT | Reject validation request |

---

## Technician Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| GET | `/technicians/dashboard` | TECHNICIAN | Tech-specific dashboard |
| GET | `/technicians/routes` | TECHNICIAN | Get assigned routes |
| GET | `/technicians/routes/{id}` | TECHNICIAN | Get route detail |
| PATCH | `/technicians/routes/{id}/start` | TECHNICIAN | Start route |
| PATCH | `/technicians/routes/{id}/complete` | TECHNICIAN | Complete route |
| GET | `/technicians/work-orders` | TECHNICIAN | Get work orders |
| PATCH | `/technicians/work-orders/{id}/start` | TECHNICIAN | Start work |
| PATCH | `/technicians/work-orders/{id}/complete` | TECHNICIAN | Complete work order |
| POST | `/meters/{id}/readings` | TECHNICIAN | Submit meter reading |

---

## Agency & Geolocation Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| GET | `/agencies` | ALL | List JIRAMA agencies/offices |
| GET | `/agencies/nearby` | ALL | Find nearby agencies |
| GET | `/agencies/{id}` | ALL | Get agency detail |

---

## Referral Endpoints

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| POST | `/referrals` | CUSTOMER | Create referral |
| GET | `/referrals/my` | CUSTOMER | Get referrals |
| GET | `/referrals/stats` | CUSTOMER | Get referral statistics |
| GET | `/referrals/leaderboard` | CUSTOMER | Referral leaderboard |

---

## OpenAPI / Swagger

- **URL:** `/swagger-ui.html`
- **Spec:** `/api-docs`
- **Auth:** Swagger UI supports Keycloak OAuth2 flow for testing

## Rate Limiting

| Endpoint Group | Limit | Period |
|---|---|---|
| Public endpoints | 100 requests | 1 minute |
| Authenticated endpoints | 500 requests | 1 minute |
| Payment endpoints | 30 requests | 1 minute |
| Report generation | 10 requests | 1 hour |
