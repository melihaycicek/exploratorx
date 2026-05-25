# ExploratorX — Demo Flow

> Skeleton document. Expanded in later phases.

## Prerequisites

1. `docker compose up -d`
2. Wait ~30s for health checks.
3. Register Debezium connectors (see README).
4. Open dashboard at http://localhost:3000.

## Scenario buttons

### CDR
- Start CDR Normal Flow
- Start CDR Suspicious Movement
- Start CDR Impossible Signal
- Start CDR Split Signal

### Payment
- Start Payment Normal Flow
- Start Payment Impossible Transaction
- Start Duplicate Payment
- Start Velocity Fraud
- Start 3DS Challenge Scenario

### Historical
- Run CDR Historical Backfill
- Run Payment Historical Backfill

### Control
- Reset Demo

## Expected results

| Scenario             | Map outcome                                            |
| -------------------- | ------------------------------------------------------ |
| Normal               | Green markers/routes, no alerts                        |
| Suspicious Movement  | Yellow markers, REVIEW_REQUIRED alerts                 |
| Impossible Signal    | Red route, IMPOSSIBLE_SIGNAL alert                     |
| Split Signal         | Purple pulsing markers at same timestamp               |
| Impossible Card      | Red route, BLOCKED transaction                         |
| Duplicate Payment    | Gray marker, DUPLICATE_IGNORED                         |
| Velocity Fraud       | Cluster of markers, escalating risk                    |
| 3DS Challenge        | Orange marker, CHALLENGE_REQUIRED                      |
| Historical Backfill  | Old synthetic records replay; historical routes appear |

## TODO

- Step-by-step screenshots
- Demo script / narration
