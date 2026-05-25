# ExploratorX — CDR Mode

> Skeleton document. Expanded in later phases.

## Purpose

Detect physically impossible subscriber mobility in a synthetic German telecom CDR
signal stream. Example: the same subscriber appears in Berlin and Hamburg within
3 minutes.

## State

`subscriber_id -> last trusted signal` (Kafka Streams RocksDB store, keyed by
`subscriber_id`).

## Core algorithm

```text
distance_km        = haversine(lat1, lon1, lat2, lon2)
time_diff_hours    = time_diff_minutes / 60.0
required_speed_kmh = distance_km / time_diff_hours
```

## Rules

| Condition                              | Decision               | Risk |
| -------------------------------------- | ---------------------- | ---- |
| same timestamp + different city        | SPLIT_SIGNAL           | +90  |
| required_speed > 900 km/h              | IMPOSSIBLE_SIGNAL      | +70  |
| required_speed > 300 km/h              | SUSPICIOUS_MOVEMENT    | +40  |
| out-of-order timestamp                 | OUT_OF_ORDER_EVENT     | +30  |
| duplicate event suspicion              | (de-risk)              | -20  |

## Decision thresholds

| Score  | Decision                       |
| ------ | ------------------------------ |
| 0–30   | NORMAL                         |
| 31–60  | SUSPICIOUS_MOVEMENT            |
| 61–89  | SUSPICIOUS_MOVEMENT_HIGH       |
| 90+    | IMPOSSIBLE_SIGNAL / SPLIT_SIGNAL |

Configured thresholds (`application.yml`): impossible 900 km/h, suspicious 300 km/h,
split-signal window 60s.

## TODO

- Engine class breakdown (ImpossibleTravelCheck, SplitSignalCheck, OutOfOrderCheck)
- Edge cases and tuning
