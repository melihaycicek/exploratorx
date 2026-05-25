# ExploratorX — Payment Fraud Mode

> Skeleton document. Expanded in later phases.

## Purpose

Detect impossible card travel, duplicate payments, velocity fraud, geo mismatch, and
3DS decisions in a synthetic payment transaction stream.

> Synthetic, tokenized data only. Never store PAN, CVV, or PIN. Safe fields:
> `card_token`, `masked_pan`, `last4`.

## State

- `card_token -> last trusted transaction`
- `card_token -> velocity window`
- `card_token -> recent idempotency keys / fingerprints`

(Kafka Streams RocksDB stores, keyed by `card_token`.)

## Rules

| Condition                              | Decision           | Risk |
| -------------------------------------- | ------------------ | ---- |
| same card_token impossible travel      |                    | +80  |
| 5+ transactions / 5 minutes            |                    | +50  |
| duplicate idempotency_key              | DUPLICATE_IGNORED  | +95  |
| POS country != IP country              |                    | +30  |
| new terminal + high amount             |                    | +25  |
| 5+ failed auth attempts / 10 minutes   |                    | +45  |

## Decision thresholds

| Score | Decision           |
| ----- | ------------------ |
| 0–30  | APPROVED           |
| 31–60 | REVIEW_REQUIRED    |
| 61–80 | CHALLENGE_REQUIRED |
| 81+   | BLOCKED            |

Configured thresholds (`application.yml`): impossible 500 km/h, velocity window 5 min,
velocity max 5 transactions.

## TODO

- Engine class breakdown (ImpossibleCardTravelCheck, VelocityFraudCheck,
  DuplicatePaymentCheck, GeoMismatchCheck, ThreeDSDecisionCheck)
- 3DS decision matrix
