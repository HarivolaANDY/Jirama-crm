# PostgreSQL Data Model

## Entity-Relationship Overview

```
┌───────────────┐     ┌──────────────────┐     ┌──────────────────┐
│    regions    │─────│    subscribers    │─────│    contracts     │
└───────────────┘     └──────────────────┘     └──────────────────┘
                                                      │
                                                      │
                                                      ▼
                                               ┌──────────────┐
                                               │    meters    │
                                               └──────────────┘
                                                      │
                                                      ▼
┌───────────────┐     ┌──────────────────┐     ┌──────────────────┐
│    users      │     │    invoices      │◄────│ consumption_read │
│  (Keycloak)   │     └──────────────────┘     └──────────────────┘
└───────────────┘              │
       │                       │
       ▼                       ▼
┌───────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   agents      │     │    payments      │     │   outages        │
│  technicians  │     └──────────────────┘     └──────────────────┘
└───────────────┘                                   │
                                                    │
┌───────────────┐     ┌──────────────────┐          │
│  complaints   │     │   incidents      │◄─────────┘
└───────────────┘     └──────────────────┘
                                                    │
                                                    ▼
┌───────────────┐     ┌──────────────────┐     ┌──────────────────┐
│notifications  │     │   documents      │     │   audit_logs     │
└───────────────┘     └──────────────────┘     └──────────────────┘
```

## Tables

### 1. `regions` — Geographic Hierarchy

```sql
CREATE TABLE regions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(10) NOT NULL UNIQUE,    -- e.g., "ANA", "TAM", "TOA"
    name            VARCHAR(255) NOT NULL,           -- e.g., "Analamanga", "Toamasina"
    type            VARCHAR(20) NOT NULL CHECK (type IN ('province', 'region', 'district', 'commune', 'fokontany')),
    parent_id       UUID REFERENCES regions(id),
    geometry        JSONB,                          -- GeoJSON polygon
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_regions_parent ON regions(parent_id);
CREATE INDEX idx_regions_type ON regions(type);
```

### 2. `subscribers` — Customers

```sql
CREATE TABLE subscribers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscriber_number   VARCHAR(20) NOT NULL UNIQUE,     -- e.g., "JRM-2026-000001"
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(255),
    phone_number        VARCHAR(20) NOT NULL,
    secondary_phone     VARCHAR(20),
    id_card_number      VARCHAR(50),                     -- CIN / passport
    tax_id              VARCHAR(50),                     -- NIF
    address_line1       VARCHAR(255) NOT NULL,
    address_line2       VARCHAR(255),
    city                VARCHAR(100) NOT NULL,
    district            VARCHAR(100),
    region_id           UUID REFERENCES regions(id),
    postal_code         VARCHAR(20),
    latitude            DECIMAL(10, 7),
    longitude           DECIMAL(10, 7),
    status              VARCHAR(20) NOT NULL DEFAULT 'active'
                        CHECK (status IN ('active', 'inactive', 'suspended', 'blacklisted')),
    subscriber_type     VARCHAR(20) NOT NULL DEFAULT 'residential'
                        CHECK (subscriber_type IN ('residential', 'commercial', 'industrial', 'government')),
    preferred_language  VARCHAR(5) DEFAULT 'fr',
    keycloak_user_id    VARCHAR(255),                    -- Link to Keycloak user
    created_by          UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at          TIMESTAMPTZ                      -- Soft delete
);

CREATE INDEX idx_subscribers_number ON subscribers(subscriber_number);
CREATE INDEX idx_subscribers_phone ON subscribers(phone_number);
CREATE INDEX idx_subscribers_email ON subscribers(email);
CREATE INDEX idx_subscribers_region ON subscribers(region_id);
CREATE INDEX idx_subscribers_status ON subscribers(status);
CREATE INDEX idx_subscribers_type ON subscribers(subscriber_type);
CREATE INDEX idx_subscribers_deleted ON subscribers(deleted_at) WHERE deleted_at IS NULL;
```

### 3. `contracts` — Service Contracts

```sql
CREATE TABLE contracts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_number     VARCHAR(20) NOT NULL UNIQUE,
    subscriber_id       UUID NOT NULL REFERENCES subscribers(id),
    contract_type       VARCHAR(20) NOT NULL
                        CHECK (contract_type IN ('electricity', 'water', 'both')),
    status              VARCHAR(20) NOT NULL DEFAULT 'active'
                        CHECK (status IN ('active', 'suspended', 'terminated', 'pending')),
    start_date          DATE NOT NULL,
    end_date            DATE,
    termination_reason  TEXT,
    connection_power    DECIMAL(10, 2),                 -- kVA for electricity
    water_flow_rate     DECIMAL(10, 2),                 -- m³/h for water
    tariff_code         VARCHAR(20) NOT NULL,            -- e.g., "T1", "T2", "BT1"
    billing_cycle       VARCHAR(20) NOT NULL DEFAULT 'monthly'
                        CHECK (billing_cycle IN ('monthly', 'bimonthly')),
    deposit_amount      DECIMAL(12, 2),
    notes               TEXT,
    created_by          UUID NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX idx_contracts_subscriber ON contracts(subscriber_id);
CREATE INDEX idx_contracts_status ON contracts(status);
CREATE INDEX idx_contracts_type ON contracts(contract_type);
CREATE UNIQUE INDEX idx_contracts_active_unique ON contracts(subscriber_id)
    WHERE status = 'active' AND contract_type = 'electricity';
```

### 4. `meters` — Utility Meters

```sql
CREATE TABLE meters (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meter_number        VARCHAR(30) NOT NULL UNIQUE,
    contract_id         UUID NOT NULL REFERENCES contracts(id),
    meter_type          VARCHAR(20) NOT NULL
                        CHECK (meter_type IN ('electricity', 'water')),
    model               VARCHAR(100),
    manufacturer        VARCHAR(100),
    installation_date   DATE NOT NULL,
    last_reading_date   TIMESTAMPTZ,
    last_reading_value  DECIMAL(12, 2),
    initial_reading     DECIMAL(12, 2) DEFAULT 0,
    multiplier_factor   DECIMAL(10, 2) DEFAULT 1.00,
    location_lat        DECIMAL(10, 7),
    location_lng        DECIMAL(10, 7),
    status              VARCHAR(20) NOT NULL DEFAULT 'active'
                        CHECK (status IN ('active', 'inactive', 'faulty', 'stolen', 'replaced')),
    seal_number         VARCHAR(50),
    qr_code             VARCHAR(255),                   -- QR code for scanning
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_meters_contract ON meters(contract_id);
CREATE INDEX idx_meters_status ON meters(status);
CREATE INDEX idx_meters_type ON meters(meter_type);
CREATE INDEX idx_meters_qr ON meters(qr_code);
```

### 5. `consumption_readings` — Meter Readings

```sql
CREATE TABLE consumption_readings (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meter_id            UUID NOT NULL REFERENCES meters(id),
    reading_date        TIMESTAMPTZ NOT NULL DEFAULT now(),
    reading_value       DECIMAL(12, 2) NOT NULL,
    consumption         DECIMAL(12, 2) NOT NULL,         -- Calculated: reading - previous_reading
    previous_reading    DECIMAL(12, 2) NOT NULL,
    reading_source      VARCHAR(20) NOT NULL DEFAULT 'manual'
                        CHECK (reading_source IN ('manual', 'smart_meter', 'estimated', 'self_report')),
    reader_id           UUID REFERENCES users(id),       -- Technician who took the reading
    is_estimated        BOOLEAN NOT NULL DEFAULT false,
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_readings_meter ON consumption_readings(meter_id);
CREATE INDEX idx_readings_date ON consumption_readings(reading_date);
CREATE INDEX idx_readings_meter_date ON consumption_readings(meter_id, reading_date DESC);
```

### 6. `invoices` — Bills

```sql
CREATE TABLE invoices (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number      VARCHAR(30) NOT NULL UNIQUE,     -- e.g., "FAC-2026-06-000001"
    contract_id         UUID NOT NULL REFERENCES contracts(id),
    subscriber_id       UUID NOT NULL REFERENCES subscribers(id),
    billing_period_start DATE NOT NULL,
    billing_period_end   DATE NOT NULL,
    issue_date          DATE NOT NULL,
    due_date            DATE NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'pending'
                        CHECK (status IN ('pending', 'paid', 'overdue', 'cancelled', 'partially_paid')),
    consumption_kwh     DECIMAL(12, 2),                  -- Electricity consumption (kWh)
    consumption_m3      DECIMAL(12, 2),                  -- Water consumption (m³)
    
    -- Detailed line items
    subscription_fee    DECIMAL(12, 2) NOT NULL DEFAULT 0,
    energy_fee          DECIMAL(12, 2) NOT NULL DEFAULT 0,
    water_fee           DECIMAL(12, 2) NOT NULL DEFAULT 0,
    taxes               DECIMAL(12, 2) NOT NULL DEFAULT 0,
    penalties           DECIMAL(12, 2) NOT NULL DEFAULT 0,
    other_fees          DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_amount        DECIMAL(12, 2) NOT NULL,
    amount_paid         DECIMAL(12, 2) DEFAULT 0,
    balance_due         DECIMAL(12, 2) NOT NULL,
    
    pdf_path            VARCHAR(500),                    -- Path in MinIO
    tariff_code         VARCHAR(20),
    meter_reading_start DECIMAL(12, 2),
    meter_reading_end   DECIMAL(12, 2),
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoices_contract ON invoices(contract_id);
CREATE INDEX idx_invoices_subscriber ON invoices(subscriber_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoices_period ON invoices(billing_period_start, billing_period_end);
CREATE INDEX idx_invoices_overdue ON invoices(due_date, status)
    WHERE status IN ('pending', 'partially_paid');
```

### 7. `payments` — Payment Transactions

```sql
CREATE TABLE payments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_number      VARCHAR(30) NOT NULL UNIQUE,
    invoice_id          UUID NOT NULL REFERENCES invoices(id),
    subscriber_id       UUID NOT NULL REFERENCES subscribers(id),
    amount              DECIMAL(12, 2) NOT NULL,
    payment_method      VARCHAR(30) NOT NULL
                        CHECK (payment_method IN (
                            'cash', 'card', 'mobile_money', 'bank_transfer',
                            'direct_debit', 'online', 'qr_code'
                        )),
    mobile_money_provider VARCHAR(30),                    -- e.g., "Mvola", "Orange Money", "Airtel Money"
    bank_name           VARCHAR(100),
    transaction_reference VARCHAR(100),                   -- External reference
    payment_date        TIMESTAMPTZ NOT NULL DEFAULT now(),
    status              VARCHAR(20) NOT NULL DEFAULT 'completed'
                        CHECK (status IN ('pending', 'completed', 'failed', 'refunded', 'cancelled')),
    receipt_number      VARCHAR(50),
    processed_by        UUID REFERENCES users(id),       -- Agent who processed (null if self-service)
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_invoice ON payments(invoice_id);
CREATE INDEX idx_payments_subscriber ON payments(subscriber_id);
CREATE INDEX idx_payments_method ON payments(payment_method);
CREATE INDEX idx_payments_date ON payments(payment_date);
CREATE INDEX idx_payments_reference ON payments(transaction_reference);
```

### 8. `incidents` — Service Incidents

```sql
CREATE TABLE incidents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_number     VARCHAR(30) NOT NULL UNIQUE,
    subscriber_id       UUID REFERENCES subscribers(id),  -- Null if reported by agent
    incident_type       VARCHAR(30) NOT NULL
                        CHECK (incident_type IN (
                            'power_outage', 'water_outage', 'voltage_fluctuation',
                            'meter_malfunction', 'line_break', 'transformer_failure',
                            'water_leak', 'low_pressure', 'other'
                        )),
    severity            VARCHAR(20) NOT NULL DEFAULT 'medium'
                        CHECK (severity IN ('critical', 'high', 'medium', 'low')),
    status              VARCHAR(20) NOT NULL DEFAULT 'reported'
                        CHECK (status IN (
                            'reported', 'confirmed', 'assigned', 'in_progress',
                            'resolved', 'closed', 'cancelled'
                        )),
    description         TEXT NOT NULL,
    location_lat        DECIMAL(10, 7),
    location_lng        DECIMAL(10, 7),
    address             VARCHAR(255),
    region_id           UUID REFERENCES regions(id),
    affected_contracts  INTEGER DEFAULT 0,                -- Estimated affected customers
    assigned_team       UUID REFERENCES work_teams(id),
    resolution_notes    TEXT,
    resolved_at         TIMESTAMPTZ,
    reported_by         UUID NOT NULL REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_incidents_subscriber ON incidents(subscriber_id);
CREATE INDEX idx_incidents_status ON incidents(status);
CREATE INDEX idx_incidents_type ON incidents(incident_type);
CREATE INDEX idx_incidents_severity ON incidents(severity);
CREATE INDEX idx_incidents_region ON incidents(region_id);
CREATE INDEX idx_incidents_assigned ON incidents(assigned_team);
```

### 9. `outages` — Planned & Unplanned Outages

```sql
CREATE TABLE outages (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    outage_number       VARCHAR(30) NOT NULL UNIQUE,
    outage_type         VARCHAR(20) NOT NULL
                        CHECK (outage_type IN ('planned', 'unplanned', 'emergency')),
    status              VARCHAR(20) NOT NULL DEFAULT 'scheduled'
                        CHECK (status IN ('scheduled', 'active', 'resolved', 'cancelled')),
    title               VARCHAR(255) NOT NULL,
    description         TEXT NOT NULL,
    region_id           UUID REFERENCES regions(id),
    affected_areas      JSONB,                           -- Array of affected commune/district names
    affected_contracts  INTEGER,
    start_time          TIMESTAMPTZ NOT NULL,
    estimated_end_time  TIMESTAMPTZ,
    actual_end_time     TIMESTAMPTZ,
    cause               TEXT,
    resolution_notes    TEXT,
    created_by          UUID NOT NULL REFERENCES users(id),
    notified            BOOLEAN NOT NULL DEFAULT false,   -- Whether subscribers were notified
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_outages_region ON outages(region_id);
CREATE INDEX idx_outages_status ON outages(status);
CREATE INDEX idx_outages_type ON outages(outage_type);
CREATE INDEX idx_outages_start ON outages(start_time);
```

### 10. `complaints` — Customer Complaints

```sql
CREATE TABLE complaints (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    complaint_number    VARCHAR(30) NOT NULL UNIQUE,
    subscriber_id       UUID NOT NULL REFERENCES subscribers(id),
    category            VARCHAR(30) NOT NULL
                        CHECK (category IN (
                            'billing', 'meter', 'service_quality', 'customer_service',
                            'connection', 'disconnection', 'other'
                        )),
    subject             VARCHAR(255) NOT NULL,
    description         TEXT NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'open'
                        CHECK (status IN ('open', 'in_progress', 'awaiting_response',
                                          'resolved', 'closed', 'escalated')),
    priority            VARCHAR(10) NOT NULL DEFAULT 'normal'
                        CHECK (priority IN ('low', 'normal', 'high', 'urgent')),
    assigned_to         UUID REFERENCES users(id),
    resolution          TEXT,
    resolved_at         TIMESTAMPTZ,
    satisfaction_rating INTEGER CHECK (satisfaction_rating BETWEEN 1 AND 5),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_complaints_subscriber ON complaints(subscriber_id);
CREATE INDEX idx_complaints_status ON complaints(status);
CREATE INDEX idx_complaints_category ON complaints(category);
CREATE INDEX idx_complaints_assigned ON complaints(assigned_to);
```

### 11. `complaint_messages` — Threads on Complaints

```sql
CREATE TABLE complaint_messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    complaint_id    UUID NOT NULL REFERENCES complaints(id),
    sender_id       UUID NOT NULL REFERENCES users(id),
    message         TEXT NOT NULL,
    is_internal     BOOLEAN NOT NULL DEFAULT false,      -- Internal note vs customer-visible
    attachments     JSONB,                               -- Array of document IDs
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_complaint_msgs ON complaint_messages(complaint_id, created_at);
```

### 12. `users` — JIRAMA Staff (not customers)

```sql
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_number     VARCHAR(20) NOT NULL UNIQUE,
    email               VARCHAR(255) NOT NULL UNIQUE,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    phone_number        VARCHAR(20) NOT NULL,
    position            VARCHAR(100),
    department          VARCHAR(100),
    region_id           UUID REFERENCES regions(id),
    role                VARCHAR(20) NOT NULL
                        CHECK (role IN ('super_admin', 'admin', 'agent', 'technician', 'call_center', 'manager')),
    keycloak_id         VARCHAR(255) NOT NULL,            -- Keycloak user ID
    is_active           BOOLEAN NOT NULL DEFAULT true,
    last_login_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_region ON users(region_id);
CREATE UNIQUE INDEX idx_users_keycloak ON users(keycloak_id);
```

### 13. `work_teams` — Technician Teams

```sql
CREATE TABLE work_teams (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    region_id       UUID REFERENCES regions(id),
    team_leader_id  UUID REFERENCES users(id),
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE work_team_members (
    team_id     UUID NOT NULL REFERENCES work_teams(id),
    user_id     UUID NOT NULL REFERENCES users(id),
    role        VARCHAR(20) NOT NULL DEFAULT 'member'
                CHECK (role IN ('leader', 'member')),
    PRIMARY KEY (team_id, user_id)
);
```

### 14. `notifications` — Notification Queue

```sql
CREATE TABLE notifications (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id        UUID NOT NULL,                    -- Can be subscriber_id or user_id
    recipient_type      VARCHAR(20) NOT NULL
                        CHECK (recipient_type IN ('subscriber', 'user')),
    channel             VARCHAR(20) NOT NULL
                        CHECK (channel IN ('email', 'sms', 'push', 'in_app')),
    title               VARCHAR(255) NOT NULL,
    body                TEXT NOT NULL,
    template_id         UUID REFERENCES notification_templates(id),
    reference_type      VARCHAR(30),                      -- e.g., 'invoice', 'outage', 'incident'
    reference_id        UUID,
    status              VARCHAR(20) NOT NULL DEFAULT 'pending'
                        CHECK (status IN ('pending', 'sent', 'delivered', 'failed', 'read')),
    read_at             TIMESTAMPTZ,
    sent_at             TIMESTAMPTZ,
    error_message       TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_id, recipient_type);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created ON notifications(created_at DESC);
```

### 15. `notification_templates`

```sql
CREATE TABLE notification_templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(50) NOT NULL UNIQUE,          -- e.g., "BILL_REMINDER", "OUTAGE_ALERT"
    name            VARCHAR(255) NOT NULL,
    channels        JSONB NOT NULL,                       -- e.g., {"email": true, "sms": true}
    subject_fr      TEXT NOT NULL,                        -- French template
    body_fr         TEXT NOT NULL,
    subject_mg      TEXT,                                 -- Malagasy template
    body_mg         TEXT,
    variables       JSONB NOT NULL,                       -- e.g., ["subscriber_name", "amount", "due_date"]
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### 16. `documents` — Document Management (MinIO)

```sql
CREATE TABLE documents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_number     VARCHAR(30) NOT NULL UNIQUE,
    title               VARCHAR(255) NOT NULL,
    document_type       VARCHAR(30) NOT NULL
                        CHECK (document_type IN (
                            'contract', 'invoice', 'meter_certificate',
                            'id_card', 'complaint_attachment', 'incident_report',
                            'payment_receipt', 'connection_request', 'other'
                        )),
    file_name           VARCHAR(255) NOT NULL,
    file_size           BIGINT NOT NULL,                  -- Bytes
    mime_type           VARCHAR(100) NOT NULL,
    storage_path        VARCHAR(500) NOT NULL,             -- MinIO bucket/key
    bucket              VARCHAR(100) NOT NULL,
    entity_type         VARCHAR(30),                      -- e.g., 'subscriber', 'contract', 'invoice'
    entity_id           UUID,
    uploaded_by         UUID REFERENCES users(id),
    checksum            VARCHAR(64),                      -- SHA-256
    is_archived         BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_documents_entity ON documents(entity_type, entity_id);
CREATE INDEX idx_documents_type ON documents(document_type);
CREATE INDEX idx_documents_uploader ON documents(uploaded_by);
```

### 17. `audit_logs` — Immutable Audit Trail

```sql
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID,                                  -- Who performed the action
    user_role       VARCHAR(20),
    action          VARCHAR(50) NOT NULL,                  -- e.g., 'CREATE_SUBSCRIBER', 'UPDATE_CONTRACT'
    entity_type     VARCHAR(30) NOT NULL,                  -- e.g., 'subscriber', 'contract', 'invoice'
    entity_id       UUID NOT NULL,
    old_values      JSONB,                                 -- Previous state
    new_values      JSONB,                                 -- New state
    ip_address      INET,
    user_agent      VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
) PARTITION BY RANGE (created_at);

-- Create monthly partitions
CREATE TABLE audit_logs_2026_06 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
CREATE TABLE audit_logs_2026_07 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');

CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);
```

### 18. `referrals` — Customer Referral Program

```sql
CREATE TABLE referrals (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    referrer_id         UUID NOT NULL REFERENCES subscribers(id),
    referred_email      VARCHAR(255),
    referred_phone      VARCHAR(20),
    referred_name       VARCHAR(255),
    referral_code       VARCHAR(20) NOT NULL UNIQUE,
    status              VARCHAR(20) NOT NULL DEFAULT 'pending'
                        CHECK (status IN ('pending', 'converted', 'rewarded', 'expired')),
    new_subscriber_id   UUID REFERENCES subscribers(id),
    reward_amount       DECIMAL(12, 2),
    reward_claimed      BOOLEAN NOT NULL DEFAULT false,
    claimed_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_referrals_referrer ON referrals(referrer_id);
CREATE INDEX idx_referrals_code ON referrals(referral_code);
CREATE INDEX idx_referrals_status ON referrals(status);
```

### 19. `connection_requests` — New Connection Applications

```sql
CREATE TABLE connection_requests (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_number      VARCHAR(30) NOT NULL UNIQUE,
    subscriber_id       UUID REFERENCES subscribers(id),
    request_type        VARCHAR(20) NOT NULL
                        CHECK (request_type IN ('new_connection', 'reconnection', 'upgrade', 'downgrade')),
    service_type        VARCHAR(20) NOT NULL
                        CHECK (service_type IN ('electricity', 'water', 'both')),
    status              VARCHAR(20) NOT NULL DEFAULT 'submitted'
                        CHECK (status IN ('submitted', 'documents_review', 'site_inspection',
                                          'approved', 'installation_scheduled', 'completed', 'rejected')),
    applicant_name      VARCHAR(255) NOT NULL,
    applicant_phone     VARCHAR(20) NOT NULL,
    applicant_email     VARCHAR(255),
    property_address    TEXT NOT NULL,
    property_lat        DECIMAL(10, 7),
    property_lng        DECIMAL(10, 7),
    required_power      DECIMAL(10, 2),                   -- kVA
    documents_submitted JSONB,
    assigned_technician UUID REFERENCES users(id),
    inspection_date     TIMESTAMPTZ,
    inspection_notes    TEXT,
    installation_date   TIMESTAMPTZ,
    rejection_reason    TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_connection_subscriber ON connection_requests(subscriber_id);
CREATE INDEX idx_connection_status ON connection_requests(status);
CREATE INDEX idx_connection_technician ON connection_requests(assigned_technician);
```

## Database Configuration

```sql
-- pg_hba.conf: SSL required for remote connections
-- PostgreSQL config:
-- max_connections = 200
-- shared_buffers = 4GB
-- effective_cache_size = 12GB
-- work_mem = 64MB
-- maintenance_work_mem = 1GB
-- random_page_cost = 1.1
-- effective_io_concurrency = 200
-- wal_level = replica
-- max_wal_size = 4GB
-- min_wal_size = 1GB

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- Full-text search
CREATE INDEX idx_subscribers_fulltext ON subscribers
    USING GIN (to_tsvector('french', first_name || ' ' || last_name || ' ' || subscriber_number));
```

## Flyway Migration Plan

```
V1__create_regions_and_users.sql
V2__create_subscribers_and_contracts.sql
V3__create_meters_and_readings.sql
V4__create_invoices_and_payments.sql
V5__create_incidents_and_outages.sql
V6__create_complaints.sql
V7__create_notifications_and_templates.sql
V8__create_documents.sql
V9__create_audit_logs.sql
V10__create_referrals_and_connection_requests.sql
V11__create_indexes_and_performance.sql
V12__seed_data_regions.sql
```
