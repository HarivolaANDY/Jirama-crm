# Sequence Diagrams

## 1. Authentication Flow (OAuth2 + PKCE)

```mermaid
sequenceDiagram
    participant User
    participant Browser
    participant Frontend as React SPA
    participant Keycloak
    participant Backend as Spring Boot API
    participant DB as PostgreSQL

    User->>Browser: Click "Login"
    Browser->>Frontend: Navigate to /login
    Frontend->>Keycloak: Redirect to /auth/realms/jirama/protocol/openid-connect/auth
    Note over Frontend,Keycloak: code_challenge=S256(code_verifier)
    
    Keycloak->>User: Display login form
    User->>Keycloak: Enter credentials
    
    Keycloak->>Keycloak: Validate credentials
    Keycloak->>Frontend: Redirect with authorization code
    Note over Frontend,Keycloak: Redirect URI: /auth/callback?code=...
    
    Frontend->>Keycloak: POST /token (code + code_verifier)
    Keycloak->>Frontend: Access Token + Refresh Token + ID Token
    
    Frontend->>Keycloak: GET /userinfo
    Keycloak->>Frontend: User claims (roles, email, name)
    
    Frontend->>Frontend: Store tokens in memory
    Frontend->>Frontend: Extract roles from JWT
    
    alt Customer role
        Frontend->>Browser: Redirect to /customer/dashboard
    else Agent role
        Frontend->>Browser: Redirect to /agent/dashboard
    else Technician role
        Frontend->>Browser: Redirect to /technician/dashboard
    else Admin role
        Frontend->>Browser: Redirect to /admin/dashboard
    end
    
    Browser->>Frontend: /customer/dashboard
    Frontend->>Backend: GET /api/v1/subscribers/my
    Note over Frontend,Backend: Authorization: Bearer <JWT>
    
    Backend->>Backend: Validate JWT signature (JWKS)
    Backend->>Backend: Extract user ID & roles
    Backend->>DB: SELECT subscriber WHERE keycloak_id=?
    DB->>Backend: Subscriber data
    Backend->>Frontend: SubscriberResponse JSON
    
    Frontend->>Browser: Render Dashboard
```

## 2. Bill Payment Flow (Mobile Money)

```mermaid
sequenceDiagram
    participant Customer
    participant Frontend as Customer Portal
    participant Backend as Spring Boot API
    participant MobileMoney as Mobile Money API (MVola)
    participant DB as PostgreSQL
    participant MinIO

    Customer->>Frontend: View Bills
    Frontend->>Backend: GET /api/v1/invoices/my/unpaid
    Backend->>DB: SELECT invoices WHERE subscriber_id=? AND status='pending'
    DB->>Backend: Unpaid invoices list
    Backend->>Frontend: InvoiceResponse[]
    Frontend->>Customer: Display unpaid bills
    
    Customer->>Frontend: Click "Pay" on invoice FAC-2026-06-0001
    Frontend->>Frontend: Show payment method selection
    Customer->>Frontend: Select "MVola" and enter phone number
    
    Frontend->>Backend: POST /api/v1/payments/mobile-money
    Note over Frontend,Backend: { invoiceId, amount, mvolaPhone }
    
    Backend->>Backend: Generate idempotency key
    Backend->>Backend: Validate invoice is unpaid
    Backend->>Backend: Validate amount
    
    Backend->>DB: INSERT payment (status='pending')
    DB->>Backend: Payment created
    
    Backend->>MobileMoney: POST /api/v1/merchant/pay
    Note over Backend,MobileMoney: { transactionId, amount, phone, callbackUrl }
    MobileMoney->>Customer: USSD prompt "Enter PIN to confirm"
    
    Customer->>MobileMoney: Enter PIN
    MobileMoney->>MobileMoney: Process payment
    MobileMoney->>Backend: POST /api/v1/payments/mobile-money/callback
    Note over MobileMoney,Backend: { transactionId, status, reference }
    
    alt Payment Successful
        Backend->>DB: UPDATE payment (status='completed')
        Backend->>DB: UPDATE invoice (status='paid', amount_paid=amount)
        Backend->>MinIO: Generate receipt PDF
        MinIO->>Backend: Receipt stored
        
        Backend->>MobileMoney: Confirm receipt
        Backend->>Frontend: PaymentResponse (success, receiptUrl)
        Frontend->>Customer: Show success message with receipt
        
    else Payment Failed
        Backend->>DB: UPDATE payment (status='failed')
        Backend->>Frontend: PaymentResponse (failed, error message)
        Frontend->>Customer: Show error with retry option
    end
```

## 3. Incident Reporting & Resolution Flow

```mermaid
sequenceDiagram
    participant Customer
    participant Frontend
    participant Backend
    participant DB as PostgreSQL
    participant Notification
    
    Customer->>Frontend: Report incident (power outage)
    Frontend->>Frontend: Show incident form with map
    
    Customer->>Frontend: Fill form, select location on map
    
    Frontend->>Backend: POST /api/v1/incidents
    Note over Frontend,Backend: { type, severity, description, location, contractId }
    
    Backend->>Backend: Validate input
    Backend->>DB: INSERT incident (status='reported')
    DB->>Backend: Incident created
    
    Backend->>Notification: Queue alert for region supervisor
    Backend->>Frontend: IncidentResponse (created)
    Frontend->>Customer: "Incident reported - Reference: INC-2026-0001"
    
    Note over Agent,Backend: Agent sees new incident in queue
    
    Agent->>Backend: PATCH /api/v1/incidents/{id}/status (status='confirmed')
    Backend->>DB: UPDATE incident status
    Backend->>Notification: Push notification to customer
    
    Agent->>Backend: PATCH /api/v1/incidents/{id}/assign (teamId)
    Backend->>DB: UPDATE incident assigned_team
    Backend->>Notification: Notify work team
    
    Note over Technician,Backend: Technician receives work order
    
    Technician->>Backend: PATCH /api/v1/incidents/{id}/status (status='in_progress')
    Backend->>DB: UPDATE incident status
    Backend->>Notification: Notify customer "Technician dispatched"
    
    Note over Technician: Technician resolves issue in field
    
    Technician->>Backend: PATCH /api/v1/incidents/{id}/status (status='resolved')
    Note over Technician,Backend: { resolutionNotes, resolvedAt }
    Backend->>DB: UPDATE incident (status='resolved', resolved_at=now())
    Backend->>Notification: Notify customer "Incident resolved"
    
    Customer->>Frontend: View incident status
    Frontend->>Backend: GET /api/v1/incidents/{id}
    Backend->>DB: SELECT incident
    DB->>Backend: Incident with status='resolved'
    Backend->>Frontend: IncidentResponse
    Frontend->>Customer: "Resolved ✓"
```

## 4. Invoice Generation (Batch Job)

```mermaid
sequenceDiagram
    participant Scheduler as Cron Scheduler
    participant BatchJob as Invoice Generator
    participant DB as PostgreSQL
    participant MinIO
    participant Notification

    Scheduler->>BatchJob: Trigger (1st of each month @ 02:00)
    
    BatchJob->>DB: SELECT active contracts with monthly billing
    DB->>BatchJob: Contracts list (10,000+)
    
    loop For each contract
        BatchJob->>DB: SELECT latest meter reading
        DB->>BatchJob: Consumption data
        
        BatchJob->>BatchJob: Calculate consumption
        BatchJob->>BatchJob: Apply tariff rates
        BatchJob->>BatchJob: Calculate taxes & fees
        BatchJob->>BatchJob: Compute total amount
        
        BatchJob->>DB: INSERT invoice (status='pending')
        
        BatchJob->>MinIO: Generate PDF invoice
        MinIO->>BatchJob: PDF stored (path returned)
        
        BatchJob->>DB: UPDATE invoice (pdf_path)
    end
    
    BatchJob->>DB: INSERT batch_run_log (total, success, failed)
    
    BatchJob->>Notification: Queue billing notifications
    Notification->>Customer: Email/SMS "Your bill for June is available"
    
    Note over BatchJob: Batch complete. Log results.
```

## 5. Technician Route & Meter Reading Flow

```mermaid
sequenceDiagram
    participant Tech as Technician
    participant App as Tech Mobile App
    participant Backend
    participant DB as PostgreSQL

    Tech->>App: Login
    App->>Backend: GET /api/v1/technicians/routes
    Backend->>DB: SELECT routes WHERE technician_id=?
    DB->>Backend: Today's route with 15 meters
    
    Backend->>App: RouteResponse (optimized order, meters list)
    App->>Tech: Display route on map
    
    Tech->>App: Navigate to first meter
    
    Tech->>App: "Start Reading" for meter MTR-001
    App->>App: Show meter info + last reading
    
    Tech->>App: Enter reading value: 4521.7
    App->>App: Validate reading > previous_reading (4489.2)
    
    Tech->>App: Submit reading
    Note over App,Backend: Offline-first: stored in IndexedDB if offline
    App->>Backend: POST /api/v1/meters/{id}/readings
    Note over App,Backend: { readingValue, readingDate, notes }
    
    Backend->>Backend: Calculate consumption
    Backend->>DB: INSERT consumption_reading
    Backend->>DB: UPDATE meter (last_reading_value, last_reading_date)
    Backend->>App: ReadingResponse (confirmed)
    
    App->>Tech: Green checkmark ✓ on meter
    
    loop All meters on route
        Tech->>App: Repeat reading process
    end
    
    Tech->>App: Mark route as completed
    App->>Backend: PATCH /api/v1/technicians/routes/{id}/complete
    Backend->>DB: UPDATE route (completed=true, completed_at=now())
    
    App->>Tech: "Route completed! 15/15 meters read."
```

## 6. Key Flow Summary Table

| Flow | Involved Actors | Key Transactions |
|---|---|---|
| **Authentication** | User, Browser, Frontend, Keycloak | Auth code exchange, Token issuance, Role extraction |
| **Bill Payment** | Customer, Frontend, Backend, MobileMoney API | Payment initiation, USSD PIN, Callback, Invoice update |
| **Incident Report** | Customer, Agent, Technician, Backend | Report → Confirm → Assign → Resolve → Notify |
| **Invoice Generation** | Scheduler, BatchJob, DB, MinIO | Batch reading, Tariff calc, PDF gen, Notifications |
| **Meter Reading** | Technician, App, Backend | Route display, Reading entry, DB update |
| **Complaint Tracking** | Customer, Agent, Backend | File → Assign → Message thread → Resolve → Rate |
| **Mass Notification** | Admin, Backend, Email/SMS | Template selection, Recipient filtering, Batch send |
