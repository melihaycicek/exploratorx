-- ════════════════════════════════════════════════════════════════════════════
-- ExploratorX — Debezium incremental snapshot signals
-- Run these to trigger an ad-hoc historical backfill. The connector must already
-- be registered and watching the corresponding signaling table.
--
--   psql -U exploratorx -d exploratorx -f infra/postgres/debezium-signal.sql
--
-- (The dashboard "Run Historical Backfill" buttons perform these inserts via the
--  engine REST API in later phases.)
-- ════════════════════════════════════════════════════════════════════════════

-- CDR historical backfill
INSERT INTO debezium_signal_cdr(id, type, data)
VALUES (
    'ad-hoc-cdr-backfill',
    'execute-snapshot',
    '{"data-collections": ["public.cdr_signal"]}'
);

-- Payment historical backfill
INSERT INTO debezium_signal_pay(id, type, data)
VALUES (
    'ad-hoc-pay-backfill',
    'execute-snapshot',
    '{"data-collections": ["public.payment_transaction"]}'
);
