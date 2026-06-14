-- V1: Create regions and users tables
-- JIRAMA CRM Database Schema
-- Target: PostgreSQL 16+

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- 1. Geographic Regions Hierarchy
-- ============================================================
CREATE TABLE regions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(10) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(20) NOT NULL CHECK (type IN ('province', 'region', 'district', 'commune', 'fokontany')),
    parent_id       UUID REFERENCES regions(id),
    geometry        JSONB,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_regions_parent ON regions(parent_id);
CREATE INDEX idx_regions_type ON regions(type);

-- ============================================================
-- 2. JIRAMA Staff Users
-- ============================================================
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
    role                VARCHAR(20) NOT NULL CHECK (role IN ('super_admin', 'admin', 'agent', 'technician', 'call_center', 'manager')),
    keycloak_id         VARCHAR(255) NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT true,
    last_login_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_region ON users(region_id);
CREATE UNIQUE INDEX idx_users_keycloak ON users(keycloak_id);
