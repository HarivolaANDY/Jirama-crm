-- V4: Create invoices and payments tables

-- ============================================================
-- 7. Invoices (Bills)
-- ============================================================
CREATE TABLE invoices (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number        VARCHAR(30) NOT NULL UNIQUE,
    contract_id           UUID NOT NULL REFERENCES contracts(id),
    subscriber_id         UUID NOT NULL REFERENCES subscribers(id),
    billing_period_start  DATE NOT NULL,
    billing_period_end    DATE NOT NULL,
    issue_date            DATE NOT NULL,
    due_date              DATE NOT NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'pending'
                          CHECK (status IN ('pending', 'paid', 'overdue', 'cancelled', 'partially_paid')),
    consumption_kwh       DECIMAL(12, 2),
    consumption_m3        DECIMAL(12, 2),
    subscription_fee      DECIMAL(12, 2) NOT NULL DEFAULT 0,
    energy_fee            DECIMAL(12, 2) NOT NULL DEFAULT 0,
    water_fee             DECIMAL(12, 2) NOT NULL DEFAULT 0,
    taxes                 DECIMAL(12, 2) NOT NULL DEFAULT 0,
    penalties             DECIMAL(12, 2) NOT NULL DEFAULT 0,
    other_fees            DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_amount          DECIMAL(12, 2) NOT NULL,
    amount_paid           DECIMAL(12, 2) DEFAULT 0,
    balance_due           DECIMAL(12, 2) NOT NULL,
    pdf_path              VARCHAR(500),
    tariff_code           VARCHAR(20),
    meter_reading_start   DECIMAL(12, 2),
    meter_reading_end     DECIMAL(12, 2),
    notes                 TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoices_contract ON invoices(contract_id);
CREATE INDEX idx_invoices_subscriber ON invoices(subscriber_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoices_period ON invoices(billing_period_start, billing_period_end);

-- ============================================================
-- 8. Payments
-- ============================================================
CREATE TABLE payments (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_number        VARCHAR(30) NOT NULL UNIQUE,
    invoice_id            UUID NOT NULL REFERENCES invoices(id),
    subscriber_id         UUID NOT NULL REFERENCES subscribers(id),
    amount                DECIMAL(12, 2) NOT NULL,
    payment_method        VARCHAR(30) NOT NULL
                          CHECK (payment_method IN ('cash', 'card', 'mobile_money', 'bank_transfer', 'direct_debit', 'online', 'qr_code')),
    mobile_money_provider VARCHAR(30),
    bank_name             VARCHAR(100),
    transaction_reference VARCHAR(100),
    payment_date          TIMESTAMPTZ NOT NULL DEFAULT now(),
    status                VARCHAR(20) NOT NULL DEFAULT 'completed'
                          CHECK (status IN ('pending', 'completed', 'failed', 'refunded', 'cancelled')),
    receipt_number        VARCHAR(50),
    processed_by          UUID REFERENCES users(id),
    notes                 TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_invoice ON payments(invoice_id);
CREATE INDEX idx_payments_subscriber ON payments(subscriber_id);
CREATE INDEX idx_payments_method ON payments(payment_method);
CREATE INDEX idx_payments_date ON payments(payment_date);
CREATE INDEX idx_payments_reference ON payments(transaction_reference);
