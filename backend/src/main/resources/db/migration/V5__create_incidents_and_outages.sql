-- V5: Create incidents, outages, work teams tables

-- ============================================================
-- 9. Incidents
-- ============================================================
CREATE TABLE incidents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_number     VARCHAR(30) NOT NULL UNIQUE,
    subscriber_id       UUID REFERENCES subscribers(id),
    incident_type       VARCHAR(30) NOT NULL
                        CHECK (incident_type IN ('power_outage', 'water_outage', 'voltage_fluctuation',
                                                  'meter_malfunction', 'line_break', 'transformer_failure',
                                                  'water_leak', 'low_pressure', 'other')),
    severity            VARCHAR(20) NOT NULL DEFAULT 'medium'
                        CHECK (severity IN ('critical', 'high', 'medium', 'low')),
    status              VARCHAR(20) NOT NULL DEFAULT 'reported'
                        CHECK (status IN ('reported', 'confirmed', 'assigned', 'in_progress',
                                          'resolved', 'closed', 'cancelled')),
    description         TEXT NOT NULL,
    location_lat        DECIMAL(10, 7),
    location_lng        DECIMAL(10, 7),
    address             VARCHAR(255),
    region_id           UUID REFERENCES regions(id),
    affected_contracts  INTEGER,
    assigned_team_id    UUID,
    resolution_notes    TEXT,
    resolved_at         TIMESTAMPTZ,
    reported_by         UUID NOT NULL REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_incidents_subscriber ON incidents(subscriber_id);
CREATE INDEX idx_incidents_status ON incidents(status);
CREATE INDEX idx_incidents_type ON incidents(incident_type);
CREATE INDEX idx_incidents_severity ON incidents(severity);
CREATE INDEX idx_incidents_region ON incidents(region_id);

-- ============================================================
-- 10. Outages
-- ============================================================
CREATE TABLE outages (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    outage_number       VARCHAR(30) NOT NULL UNIQUE,
    outage_type         VARCHAR(20) NOT NULL CHECK (outage_type IN ('planned', 'unplanned', 'emergency')),
    status              VARCHAR(20) NOT NULL DEFAULT 'scheduled'
                        CHECK (status IN ('scheduled', 'active', 'resolved', 'cancelled')),
    title               VARCHAR(255) NOT NULL,
    description         TEXT NOT NULL,
    region_id           UUID REFERENCES regions(id),
    affected_areas      JSONB,
    affected_contracts  INTEGER,
    start_time          TIMESTAMPTZ NOT NULL,
    estimated_end_time  TIMESTAMPTZ,
    actual_end_time     TIMESTAMPTZ,
    cause               TEXT,
    resolution_notes    TEXT,
    created_by          UUID NOT NULL REFERENCES users(id),
    notified            BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_outages_region ON outages(region_id);
CREATE INDEX idx_outages_status ON outages(status);
CREATE INDEX idx_outages_type ON outages(outage_type);
CREATE INDEX idx_outages_start ON outages(start_time);

-- ============================================================
-- 11. Work Teams
-- ============================================================
CREATE TABLE work_teams (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    region_id       UUID REFERENCES regions(id),
    team_leader_id  UUID REFERENCES users(id),
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE work_team_members (
    team_id     UUID NOT NULL REFERENCES work_teams(id),
    user_id     UUID NOT NULL REFERENCES users(id),
    role        VARCHAR(20) NOT NULL DEFAULT 'member' CHECK (role IN ('leader', 'member')),
    PRIMARY KEY (team_id, user_id)
);
