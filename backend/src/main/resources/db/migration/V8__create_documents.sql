-- V8: Create documents table

-- ============================================================
-- 16. Documents (MinIO Metadata Index)
-- ============================================================
CREATE TABLE documents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_number     VARCHAR(30) NOT NULL UNIQUE,
    title               VARCHAR(255) NOT NULL,
    document_type       VARCHAR(30) NOT NULL
                        CHECK (document_type IN ('contract', 'invoice', 'meter_certificate',
                                                  'id_card', 'complaint_attachment', 'incident_report',
                                                  'payment_receipt', 'connection_request', 'other')),
    file_name           VARCHAR(255) NOT NULL,
    file_size           BIGINT NOT NULL,
    mime_type           VARCHAR(100) NOT NULL,
    storage_path        VARCHAR(500) NOT NULL,
    bucket              VARCHAR(100) NOT NULL,
    entity_type         VARCHAR(30),
    entity_id           UUID,
    uploaded_by         UUID REFERENCES users(id),
    checksum            VARCHAR(64),
    is_archived         BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_documents_entity ON documents(entity_type, entity_id);
CREATE INDEX idx_documents_type ON documents(document_type);
CREATE INDEX idx_documents_uploader ON documents(uploaded_by);
