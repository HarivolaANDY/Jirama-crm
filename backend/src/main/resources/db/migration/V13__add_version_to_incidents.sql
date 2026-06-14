-- V13: Add version column for JPA optimistic locking on all entities that use @Version
ALTER TABLE incidents   ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE subscribers ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE invoices    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
