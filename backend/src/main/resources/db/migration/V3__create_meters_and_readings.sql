-- V3: Create meters and consumption readings tables

-- ============================================================
-- 5. Meters
-- ============================================================
CREATE TABLE meters (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meter_number        VARCHAR(30) NOT NULL UNIQUE,
    contract_id         UUID NOT NULL REFERENCES contracts(id),
    meter_type          VARCHAR(20) NOT NULL CHECK (meter_type IN ('electricity', 'water')),
    model               VARCHAR(100),
    manufacturer        VARCHAR(100),
    installation_date   DATE NOT NULL,
    last_reading_date   TIMESTAMPTZ,
    last_reading_value  DECIMAL(12, 2),
    initial_reading     DECIMAL(12, 2) DEFAULT 0,
    multiplier_factor   DECIMAL(10, 2) DEFAULT 1.00,
    location_lat        DECIMAL(10, 7),
    location_lng        DECIMAL(10, 7),
    status              VARCHAR(20) NOT NULL DEFAULT 'active'
                        CHECK (status IN ('active', 'inactive', 'faulty', 'stolen', 'replaced')),
    seal_number         VARCHAR(50),
    qr_code             VARCHAR(255),
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_meters_contract ON meters(contract_id);
CREATE INDEX idx_meters_status ON meters(status);
CREATE INDEX idx_meters_type ON meters(meter_type);
CREATE INDEX idx_meters_qr ON meters(qr_code);

-- ============================================================
-- 6. Consumption Readings
-- ============================================================
CREATE TABLE consumption_readings (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meter_id            UUID NOT NULL REFERENCES meters(id),
    reading_date        TIMESTAMPTZ NOT NULL DEFAULT now(),
    reading_value       DECIMAL(12, 2) NOT NULL,
    consumption         DECIMAL(12, 2) NOT NULL,
    previous_reading    DECIMAL(12, 2) NOT NULL,
    reading_source      VARCHAR(20) NOT NULL DEFAULT 'manual'
                        CHECK (reading_source IN ('manual', 'smart_meter', 'estimated', 'self_report')),
    reader_id           UUID REFERENCES users(id),
    is_estimated        BOOLEAN NOT NULL DEFAULT false,
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_readings_meter ON consumption_readings(meter_id);
CREATE INDEX idx_readings_date ON consumption_readings(reading_date);
CREATE INDEX idx_readings_meter_date ON consumption_readings(meter_id, reading_date DESC);
