-- V10: Create referrals and connection requests tables

-- ============================================================
-- 18. Referrals (Customer Referral Program)
-- ============================================================
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

-- ============================================================
-- 19. Connection Requests
-- ============================================================
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
    required_power      DECIMAL(10, 2),
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
