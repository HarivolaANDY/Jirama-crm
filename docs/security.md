# Security Architecture

## Identity & Access Management (Keycloak)

### Realm Structure

```
JIRAMA Realm
├── Roles
│   ├── CUSTOMER          → Portal self-service users
│   ├── AGENT             → JIRAMA call center & counter agents
│   ├── TECHNICIAN        → Field technicians
│   ├── MANAGER           → Department/regional managers
│   └── ADMIN             → System administrators
│
├── Groups (by region)
│   ├── Analamanga_Agent
│   ├── Analamanga_Technician
│   ├── Toamasina_Agent
│   └── ...
│
├── Clients
│   ├── jirama-frontend   → Public SPA client (PKCE)
│   └── jirama-backend    → Confidential backend client (JWT validation)
│
└── Authentication
    ├── Password policy: 8+ chars, 1 uppercase, 1 number, 1 special
    ├── 2FA via TOTP (optional for staff)
    ├── Session timeout: 8 hours (staff), 30 days (customers)
    └── Brute-force detection: 5 failed attempts → 15 min lockout
```

### Role Permissions Matrix

| Resource | CUSTOMER | AGENT | TECHNICIAN | MANAGER | ADMIN |
|---|---|---|---|---|---|
| View own profile | ✅ | ✅ | ✅ | ✅ | ✅ |
| View own bills | ✅ | - | - | - | - |
| Pay own bills | ✅ | - | - | - | - |
| View all subscribers | - | ✅ (region) | ✅ (assigned) | ✅ | ✅ |
| Manage subscribers | - | ✅ | - | - | ✅ |
| View all contracts | - | ✅ (region) | ✅ (assigned) | ✅ | ✅ |
| Manage contracts | - | ✅ | - | - | ✅ |
| View all meters | - | ✅ (region) | ✅ (assigned) | ✅ | ✅ |
| Manage meters | - | ✅ | ✅ | - | ✅ |
| Take readings | - | - | ✅ | - | - |
| Generate invoices | - | ✅ | - | - | ✅ |
| View all payments | - | ✅ (region) | - | ✅ | ✅ |
| Process payments | ✅ (own) | ✅ | - | - | ✅ |
| View incidents | ✅ (own) | ✅ (region) | ✅ (assigned) | ✅ | ✅ |
| Manage incidents | - | ✅ | ✅ (assigned) | - | ✅ |
| View complaints | - | ✅ (region) | - | ✅ | ✅ |
| Manage complaints | - | ✅ | - | - | ✅ |
| Manage users | - | - | - | - | ✅ |
| View reports | - | - | - | ✅ | ✅ |
| View audit logs | - | - | - | ✅ | ✅ |
| System config | - | - | - | - | ✅ |
| Send notifications | - | ✅ | - | ✅ | ✅ |

### JWT Token Structure

```json
{
  "iss": "https://keycloak.jirama.mg/realms/jirama",
  "sub": "user-uuid",
  "aud": "jirama-backend",
  "exp": 1718371200,
  "iat": 1718367600,
  "preferred_username": "jean.rakoto",
  "email": "jean.rakoto@email.com",
  "realm_access": {
    "roles": ["CUSTOMER"]
  },
  "resource_access": {
    "jirama-backend": {
      "roles": ["view_subscribers", "pay_bills"]
    }
  },
  "group_membership": ["Analamanga"],
  "employee_id": "EMP-00123"
}
```

## Security Layers

### 1. Network Security

```
Internet
    │
    ▼
┌────────────────────┐
│   Cloudflare/WAF    │  ← DDoS protection, IP filtering
└─────────┬──────────┘
          │
┌─────────▼──────────┐
│      Nginx          │  ← TLS termination, rate limiting, reverse proxy
└─────────┬──────────┘
          │
┌─────────▼──────────┐
│   Docker Network    │  ← Internal network (172.x.x.x)
│   (Internal Only)   │
├────────────────────┤
│  backend:8080      │  ← No public exposure
│  keycloak:8080     │  ← Internal admin only
│  postgres:5432     │  ← No public exposure
│  redis:6379        │  ← No public exposure, requires AUTH
│  minio:9000        │  ← No public exposure
│  grafana:3000      │  ← Admin VPN only
└────────────────────┘
```

### 2. Application Security

| Concern | Implementation |
|---|---|
| **SQL Injection** | JPA Parameterized Queries, Input Validation |
| **XSS** | React's built-in escaping, CSP Headers |
| **CSRF** | Stateless JWT (no cookies), SameSite cookies for OAuth2 |
| **Clickjacking** | `X-Frame-Options: DENY` |
| **CORS** | Whitelist only frontend domain |
| **HSTS** | `Strict-Transport-Security: max-age=31536000` |
| **Content Security Policy** | Restrict script sources, inline scripts with nonces |
| **Rate Limiting** | Per-IP and per-user rate limiting (Redis-backed) |
| **Request Size** | Max 10MB request body, 50MB file upload |

### 3. Data Security

| Category | Measure |
|---|---|
| **At Rest** | PostgreSQL TDE, MinIO SSE-S3 encryption |
| **In Transit** | TLS 1.3 for all external communications |
| **Passwords** | Bcrypt (cost 12) — stored only in Keycloak |
| **PII** | Email, phone, ID card number encrypted at column level using pgcrypto |
| **Payment Data** | PCI-DSS scope minimized — use Mobile Money APIs directly |
| **Audit Trail** | Immutable audit logs (append-only) |

### 4. API Security

```
Client → [Rate Limiter] → [JWT Validator] → [Role Checker] → [Controller]
```

- **JWT Validation:** Every request validates JWT signature, expiry, issuer, audience
- **Scope Check:** Spring `@PreAuthorize` on every endpoint
- **Input Validation:** Jakarta Validation on DTOs + custom validators
- **Idempotency:** Payment endpoints require `Idempotency-Key` header
- **Sensitive Data:** PII fields filtered in responses based on role

## Security Configuration (Spring Boot)

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://keycloak.jirama.mg/realms/jirama
          jwk-set-uri: https://keycloak.jirama.mg/realms/jirama/protocol/openid-connect/certs

server:
  ssl:
    enabled: true
  http2:
    enabled: true
  
# Security headers (Nginx also configured)
  servlet:
    session:
      cookie:
        secure: true
        http-only: true
        same-site: strict
```

## Frontend Security

```typescript
// keycloak.ts — Keycloak client configuration
const keycloak = new Keycloak({
  url: 'https://keycloak.jirama.mg',
  realm: 'jirama',
  clientId: 'jirama-frontend',
  pkceMethod: 'S256',          // PKCE for SPA
  onLoad: 'check-sso',
  silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html'
});

// Axios interceptor — attach token & handle 401
apiClient.interceptors.request.use(async (config) => {
  if (keycloak.authenticated) {
    if (keycloak.isTokenExpired(30)) {
      await keycloak.updateToken(30);
    }
    config.headers.Authorization = `Bearer ${keycloak.token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      keycloak.login();
    }
    return Promise.reject(error);
  }
);
```

## Compliance

| Requirement | Implementation |
|---|---|
| **RGPD (EU equivalent)** | Consent management, data export, right to deletion |
| **Data Localization** | All servers hosted in Madagascar (or cloud with data residency) |
| **Law 2014-038 (Madagascar)** | Personal data protection law compliance |
| **PCI-DSS** | Use Mobile Money SDKs — minimize card data handling |
| **Audit Trail** | All admin actions logged with immutable audit trail |
| **Retention** | Invoices: 10 years, Audit logs: 5 years, Session data: 30 days |
