-- V11: Performance indexes and full-text search

-- ============================================================
-- Full-Text Search Indexes
-- ============================================================

-- Subscriber search (French text)
CREATE INDEX idx_subscribers_fulltext ON subscribers
    USING GIN (to_tsvector(        'simple', coalesce(first_name, '') || ' ' ||
                                        coalesce(last_name, '') || ' ' ||
                                        coalesce(subscriber_number, '') || ' ' ||
                                        coalesce(phone_number, '')));

-- ============================================================
-- Composite Partial Indexes for Common Queries
-- ============================================================

-- Overdue invoices (most queried status)
CREATE INDEX idx_invoices_overdue ON invoices(due_date, status)
    WHERE status IN ('pending', 'partially_paid');

-- Active contracts only
CREATE INDEX idx_contracts_active ON contracts(subscriber_id, contract_type)
    WHERE status = 'active';

-- Active meters only
CREATE INDEX idx_meters_active ON meters(contract_id, meter_type)
    WHERE status = 'active';

-- Recent incidents (last 30 days)
CREATE INDEX idx_incidents_recent ON incidents(created_at DESC, status);

-- ============================================================
-- JSONB Indexes
-- ============================================================
CREATE INDEX idx_outages_affected_areas ON outages USING GIN (affected_areas);
CREATE INDEX idx_audit_new_values ON audit_logs USING GIN (new_values);
CREATE INDEX idx_audit_old_values ON audit_logs USING GIN (old_values);

-- ============================================================
-- Partial Index for Soft Deletes
-- ============================================================
CREATE INDEX idx_subscribers_active ON subscribers(id, subscriber_number)
    WHERE deleted_at IS NULL;
