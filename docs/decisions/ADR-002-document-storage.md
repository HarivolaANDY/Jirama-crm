# ADR-002: Document Storage Strategy

## Status
Accepted

## Context
JIRAMA needs to store various documents: invoice PDFs, meter certificates, customer ID copies, complaint attachments, incident photos. Requirements:
- Scalable to millions of documents
- Secure access control
- Efficient retrieval
- Backup and disaster recovery
- Served to customers and staff via the web

## Decision
Use **MinIO** (S3-compatible object storage) as the primary document store, with PostgreSQL for metadata indexing.

## Rationale
1. **Self-hosted:** Data sovereignty in Madagascar
2. **S3-compatible:** Standard API, works with any S3 client
3. **Cost-effective:** No per-GB pricing like AWS S3
4. **Performance:** Direct MinIO access for downloads via presigned URLs
5. **Backup:** Easy rsync/restic backups of MinIO data directory

## Implementation

### Bucket Structure
```
jirama-documents/
├── invoices/           # Generated invoice PDFs
│   └── YYYY/MM/
│       └── FAC-2026-06-000001.pdf
├── contracts/          # Signed contracts
├── id-cards/           # Customer ID cards
├── complaints/         # Complaint attachments
│   └── {complaintId}/
├── incidents/          # Incident photos
│   └── {incidentId}/
├── meter-certificates/ # Meter calibration docs
└── reports/            # Generated reports
    └── YYYY/MM/
```

### Access Control
- Documents are private by default
- Presigned URLs with 1-hour expiry
- Backend validates user permissions before generating URL
- Direct access forbidden via Nginx rules

### Metadata Table
PostgreSQL `documents` table stores: id, type, file_name, file_size, mime_type, storage_path, entity_type, entity_id, checksum (SHA-256), uploaded_by, created_at.

## Consequences
- No vendor lock-in (S3 standard)
- Reduced load on PostgreSQL (no BLOB storage)
- Presigned URLs bypass backend for downloads (scalable)
- Need to secure MinIO access keys
- Additional service to maintain (backups, updates)
