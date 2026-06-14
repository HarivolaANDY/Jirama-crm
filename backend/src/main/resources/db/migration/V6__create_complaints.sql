-- V6: Create complaints and complaint messages tables

-- ============================================================
-- 12. Complaints
-- ============================================================
CREATE TABLE complaints (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    complaint_number    VARCHAR(30) NOT NULL UNIQUE,
    subscriber_id       UUID NOT NULL REFERENCES subscribers(id),
    category            VARCHAR(30) NOT NULL
                        CHECK (category IN ('billing', 'meter', 'service_quality', 'customer_service',
                                            'connection', 'disconnection', 'other')),
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

-- ============================================================
-- 13. Complaint Messages (Thread)
-- ============================================================
CREATE TABLE complaint_messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    complaint_id    UUID NOT NULL REFERENCES complaints(id),
    sender_id       UUID NOT NULL REFERENCES users(id),
    message         TEXT NOT NULL,
    is_internal     BOOLEAN NOT NULL DEFAULT false,
    attachments     JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_complaint_msgs ON complaint_messages(complaint_id, created_at);
