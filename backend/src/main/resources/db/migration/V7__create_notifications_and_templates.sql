-- V7: Create notifications and notification templates tables

-- ============================================================
-- 14. Notification Templates
-- ============================================================
CREATE TABLE notification_templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(50) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    channels        JSONB NOT NULL,
    subject_fr      TEXT NOT NULL,
    body_fr         TEXT NOT NULL,
    subject_mg      TEXT,
    body_mg         TEXT,
    variables       JSONB NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- 15. Notifications
-- ============================================================
CREATE TABLE notifications (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id        UUID NOT NULL,
    recipient_type      VARCHAR(20) NOT NULL CHECK (recipient_type IN ('subscriber', 'user')),
    channel             VARCHAR(20) NOT NULL CHECK (channel IN ('email', 'sms', 'push', 'in_app')),
    title               VARCHAR(255) NOT NULL,
    body                TEXT NOT NULL,
    template_id         UUID REFERENCES notification_templates(id),
    reference_type      VARCHAR(30),
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

-- Seed notification templates
INSERT INTO notification_templates (code, name, channels, subject_fr, body_fr, variables, is_active) VALUES
    ('BILL_REMINDER', 'Rappel de facture', '{"email": true, "sms": true}',
     'Rappel : Votre facture JIRAMA est disponible',
     'Bonjour {{subscriber_name}}, votre facture de {{amount}} Ariary pour la période {{billing_period}} est disponible. Date limite : {{due_date}}.',
     '["subscriber_name", "amount", "billing_period", "due_date"]', true),
    ('PAYMENT_CONFIRMATION', 'Confirmation de paiement', '{"email": true, "sms": true, "in_app": true}',
     'Confirmation de votre paiement JIRAMA',
     'Votre paiement de {{amount}} Ariary a été reçu. Référence : {{payment_reference}}. Merci de votre confiance.',
     '["amount", "payment_reference"]', true),
    ('OUTAGE_ALERT', 'Alerte de coupure', '{"email": true, "sms": true, "push": true}',
     'Alerte : Coupure {{outage_type}} dans votre zone',
     'Une {{outage_type}} est prévue dans votre zone du {{start_time}} au {{estimated_end_time}}. Motif : {{cause}}.',
     '["outage_type", "start_time", "estimated_end_time", "cause"]', true),
    ('INCIDENT_UPDATE', 'Mise à jour d''incident', '{"email": true, "in_app": true}',
     'Mise à jour de votre incident {{incident_number}}',
     'Votre incident {{incident_number}} ({{incident_type}}) est maintenant {{status}}.',
     '["incident_number", "incident_type", "status"]', true),
    ('INVOICE_OVERDUE', 'Facture en retard', '{"email": true, "sms": true}',
     'URGENT : Votre facture JIRAMA est en retard',
     'Bonjour {{subscriber_name}}, votre facture de {{amount}} Ariary est en retard depuis {{days_overdue}} jours. Des pénalités peuvent s''appliquer.',
     '["subscriber_name", "amount", "days_overdue"]', true);
