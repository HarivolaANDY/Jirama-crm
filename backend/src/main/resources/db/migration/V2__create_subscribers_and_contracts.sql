-- V2: Create subscribers and contracts tables

-- ============================================================
-- 3. Subscribers (Customers)
-- ============================================================
CREATE TABLE subscribers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscriber_number   VARCHAR(20) NOT NULL UNIQUE,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(255),
    phone_number        VARCHAR(20) NOT NULL,
    secondary_phone     VARCHAR(20),
    id_card_number      VARCHAR(50),
    tax_id              VARCHAR(50),
    address_line1       VARCHAR(255) NOT NULL,
    address_line2       VARCHAR(255),
    city                VARCHAR(100) NOT NULL,
    district            VARCHAR(100),
    region_code         VARCHAR(20),
    postal_code         VARCHAR(20),
    latitude            DECIMAL(10, 7),
    longitude           DECIMAL(10, 7),
    status              VARCHAR(20) NOT NULL DEFAULT 'active'
                        CHECK (status IN ('active', 'inactive', 'suspended', 'blacklisted')),
    subscriber_type     VARCHAR(20) NOT NULL DEFAULT 'residential'
                        CHECK (subscriber_type IN ('residential', 'commercial', 'industrial', 'government')),
    preferred_language  VARCHAR(5) DEFAULT 'fr',
    keycloak_user_id    UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX idx_subscribers_number ON subscribers(subscriber_number);
CREATE INDEX idx_subscribers_phone ON subscribers(phone_number);
CREATE INDEX idx_subscribers_email ON subscribers(email);
CREATE INDEX idx_subscribers_status ON subscribers(status);
CREATE INDEX idx_subscribers_type ON subscribers(subscriber_type);

-- ============================================================
-- 4. Contracts
-- ============================================================
CREATE TABLE contracts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_number     VARCHAR(20) NOT NULL UNIQUE,
    subscriber_id       UUID NOT NULL REFERENCES subscribers(id),
    contract_type       VARCHAR(20) NOT NULL CHECK (contract_type IN ('electricity', 'water', 'both')),
    status              VARCHAR(20) NOT NULL DEFAULT 'active'
                        CHECK (status IN ('active', 'suspended', 'terminated', 'pending')),
    start_date          DATE NOT NULL,
    end_date            DATE,
    termination_reason  TEXT,
    connection_power    DECIMAL(10, 2),
    water_flow_rate     DECIMAL(10, 2),
    tariff_code         VARCHAR(20) NOT NULL,
    billing_cycle       VARCHAR(20) NOT NULL DEFAULT 'monthly' CHECK (billing_cycle IN ('monthly', 'bimonthly')),
    deposit_amount      DECIMAL(12, 2),
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_contracts_subscriber ON contracts(subscriber_id);
CREATE INDEX idx_contracts_status ON contracts(status);
CREATE INDEX idx_contracts_type ON contracts(contract_type);
