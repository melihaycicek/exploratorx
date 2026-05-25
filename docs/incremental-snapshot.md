# ExploratorX — Incremental Snapshot (Historical Backfill)

> Skeleton document. Expanded in later phases.

## Purpose

Replay historical synthetic records through the anomaly engine as a bonus demo
scene, using Debezium ad-hoc incremental snapshots. No connector restart required.

## How it works

Debezium watches a per-mode signaling table. Inserting an `execute-snapshot` signal
triggers a chunked re-read of the target table, emitting the rows onto the raw topic,
where the engine processes them like live events.

## CDR backfill signal

```sql
INSERT INTO debezium_signal_cdr(id, type, data)
VALUES (
    'ad-hoc-cdr-backfill',
    'execute-snapshot',
    '{"data-collections": ["public.cdr_signal"]}'
);
```

## Payment backfill signal

```sql
INSERT INTO debezium_signal_pay(id, type, data)
VALUES (
    'ad-hoc-pay-backfill',
    'execute-snapshot',
    '{"data-collections": ["public.payment_transaction"]}'
);
```

These statements are also collected in
[infra/postgres/debezium-signal.sql](../infra/postgres/debezium-signal.sql).

## Dashboard button

**Run Historical Backfill** (CDR / Payment).

## Expected demo result

- Old synthetic CDR/payment records flow again.
- The anomaly engine processes them.
- The Germany map shows historical impossible movements.

## TODO

- REST endpoint wiring (`POST /api/demo/backfill/cdr`, `/pay`)
- Idempotency / re-run behavior
