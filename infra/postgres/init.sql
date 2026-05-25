-- ════════════════════════════════════════════════════════════════════════════
-- ExploratorX — PostgreSQL schema & roles
-- Runs once on first container boot (docker-entrypoint-initdb.d).
-- Requires logical replication: the container is started with
--   -c wal_level=logical -c max_replication_slots=10 -c max_wal_senders=10
-- ════════════════════════════════════════════════════════════════════════════

-- ─────────────────────────────── Roles ───────────────────────────────
-- Application user (read/write via JPA)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'exploratorx') THEN
        CREATE ROLE exploratorx WITH LOGIN PASSWORD 'exploratorx';
    END IF;
END
$$;

-- Debezium CDC user (needs REPLICATION + SELECT on captured tables)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'debezium') THEN
        CREATE ROLE debezium WITH LOGIN REPLICATION PASSWORD 'debezium';
    END IF;
END
$$;

-- ─────────────────────────────── Tables ───────────────────────────────
CREATE TABLE IF NOT EXISTS cdr_signal (
    id              BIGSERIAL PRIMARY KEY,
    subscriber_id   VARCHAR(64)      NOT NULL,
    event_time      TIMESTAMPTZ      NOT NULL,
    city            VARCHAR(100)     NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    cell_id         VARCHAR(64),
    signal_type     VARCHAR(32)      DEFAULT 'VOICE',
    created_at      TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment_transaction (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id  VARCHAR(128)     NOT NULL UNIQUE,
    card_token      VARCHAR(128)     NOT NULL,
    masked_pan      VARCHAR(19),
    last4           VARCHAR(4),
    customer_id     VARCHAR(64)      NOT NULL,
    merchant_id     VARCHAR(64),
    merchant_name   VARCHAR(200),
    terminal_id     VARCHAR(64),
    channel         VARCHAR(32)      DEFAULT 'POS',
    amount          DECIMAL(12,2)    NOT NULL,
    currency        VARCHAR(3)       DEFAULT 'EUR',
    city            VARCHAR(100)     NOT NULL,
    country         VARCHAR(3)       DEFAULT 'DE',
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    event_time      TIMESTAMPTZ      NOT NULL,
    payment_status  VARCHAR(32)      DEFAULT 'PENDING',
    auth_result     VARCHAR(32),
    three_ds_status VARCHAR(32),
    device_id       VARCHAR(128),
    ip_country      VARCHAR(3),
    idempotency_key VARCHAR(128),
    created_at      TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS anomaly_log (
    id                  BIGSERIAL PRIMARY KEY,
    mode                VARCHAR(16)  NOT NULL,
    source_id           VARCHAR(128) NOT NULL,
    entity_id           VARCHAR(128),
    from_city           VARCHAR(100),
    to_city             VARCHAR(100),
    from_lat            DOUBLE PRECISION,
    from_lon            DOUBLE PRECISION,
    to_lat              DOUBLE PRECISION,
    to_lon              DOUBLE PRECISION,
    time_diff_minutes   DOUBLE PRECISION,
    distance_km         DOUBLE PRECISION,
    required_speed_kmh  DOUBLE PRECISION,
    risk_score          INT,
    decision            VARCHAR(32),
    reason              TEXT,
    raw_payload         JSONB,
    detected_at         TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS demo_run (
    id          BIGSERIAL PRIMARY KEY,
    run_id      VARCHAR(64) NOT NULL UNIQUE,
    mode        VARCHAR(16),
    scenario    VARCHAR(64),
    started_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    ended_at    TIMESTAMPTZ,
    status      VARCHAR(16) DEFAULT 'RUNNING'
);

-- Separate Debezium signaling tables for CDR and Payment
CREATE TABLE IF NOT EXISTS debezium_signal_cdr (
    id      VARCHAR(42) PRIMARY KEY,
    type    VARCHAR(32) NOT NULL,
    data    VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS debezium_signal_pay (
    id      VARCHAR(42) PRIMARY KEY,
    type    VARCHAR(32) NOT NULL,
    data    VARCHAR(2048)
);

-- ─────────────────────────────── Indexes ──────────────────────────────
CREATE INDEX IF NOT EXISTS idx_cdr_subscriber ON cdr_signal(subscriber_id, event_time DESC);
CREATE INDEX IF NOT EXISTS idx_pay_card       ON payment_transaction(card_token, event_time DESC);
CREATE INDEX IF NOT EXISTS idx_anomaly_mode   ON anomaly_log(mode, detected_at DESC);
CREATE INDEX IF NOT EXISTS idx_anomaly_source ON anomaly_log(source_id, detected_at DESC);

-- ─────────────────────────────── Grants ───────────────────────────────
-- Application user: full DML on app tables
GRANT USAGE ON SCHEMA public TO exploratorx;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO exploratorx;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO exploratorx;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO exploratorx;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO exploratorx;

-- Debezium user: SELECT on captured tables + write to its signaling tables
GRANT USAGE ON SCHEMA public TO debezium;
GRANT SELECT ON cdr_signal, payment_transaction TO debezium;
GRANT SELECT, INSERT, UPDATE ON debezium_signal_cdr, debezium_signal_pay TO debezium;
