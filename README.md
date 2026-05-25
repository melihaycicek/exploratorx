# ExploratorX

> Codename: **DuruGörü**
> Real-time anomaly exploration engine for telecom and payment event streams.

---

## What is ExploratorX?

ExploratorX is an event-driven anomaly exploration platform. It detects physically
impossible movement and fraud patterns in **synthetic** telecom and payment streams.

It runs in two modes that share the same infrastructure and the same engine:

- **CDR Mode** — synthetic German telecom CDR signal stream; detects impossible
  subscriber mobility (e.g. the same subscriber appears in Berlin and Hamburg within
  3 minutes).
- **Payment Mode** — synthetic payment transaction stream; detects impossible card
  travel, duplicate payments, and velocity fraud (e.g. the same card token used in
  Berlin and Hamburg within 3 minutes).

> All data is synthetic. See the [Safety / Synthetic Data Disclaimer](#safety--synthetic-data-disclaimer).

---

## Architecture

```text
┌─────────────┐   logical    ┌──────────────┐   raw      ┌────────────────────┐
│ PostgreSQL  │─ replication ─│   Debezium   │─ topics ──▶│ exploratorx-engine │
│ (cdr/pay)   │   (pgoutput)  │ Kafka Connect│            │  Kafka Streams +   │
└─────────────┘               └──────────────┘            │  CDR/Pay engines   │
                                                          │  REST + WebSocket  │
                              ┌──────────────┐  clean /   │  RocksDB state     │
                              │    Kafka     │◀ decision ─┤  Actuator metrics  │
                              │  (KRaft)     │   topics    └─────────┬──────────┘
                              └──────────────┘                       │ STOMP/WS
                                     ▲                                ▼
                              ┌──────────────┐            ┌────────────────────┐
                              │  Kafka UI    │            │ exploratorx-dashboard
                              └──────────────┘            │  Next.js + Leaflet │
                                                          └────────────────────┘

   Observability (optional): Prometheus scrapes the engine; Grafana dashboards.
```

> Detailed diagrams live in [docs/architecture.md](docs/architecture.md).

---

## Services

| Service                  | Description                                | Default URL / Port           |
| ------------------------ | ------------------------------------------ | ---------------------------- |
| `exploratorx-postgres`   | PostgreSQL with logical replication (WAL)  | `localhost:5432`             |
| `exploratorx-kafka`      | Kafka broker in KRaft mode (no ZooKeeper)  | `localhost:29092` (host)     |
| `exploratorx-connect`    | Debezium / Kafka Connect                   | http://localhost:8083        |
| `exploratorx-kafka-ui`   | Kafka UI                                   | http://localhost:8090        |
| `exploratorx-engine`     | Spring Boot engine (REST, WS, streams)     | http://localhost:8080        |
| `exploratorx-dashboard`  | Next.js dashboard                          | http://localhost:3000        |
| `exploratorx-prometheus` | Prometheus (observability profile)         | http://localhost:9090        |
| `exploratorx-grafana`    | Grafana (observability profile)            | http://localhost:3001        |

---

## Quick Start

Start the MVP stack:

```bash
docker compose up -d
```

Start MVP **plus** observability:

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d
```

### Boot Sequence

1. Run `docker compose up -d` to start all services.
2. Wait ~30 seconds for container health checks to pass.
3. Register Debezium connectors manually using the provided curl commands (below).
4. Open the Dashboard at http://localhost:3000.
5. Open Kafka UI at http://localhost:8090.
6. Verify Engine API at http://localhost:8080/actuator/health.
7. Click the **"Start Demo"** button on the Dashboard.

---

## Debezium Connector Registration

Connectors are **not** auto-registered. Register them once Connect is healthy:

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @infra/debezium/connectors/exploratorx-cdr-connector.json

curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @infra/debezium/connectors/exploratorx-pay-connector.json
```

Notes:
- The MVP does **not** use the `ExtractNewRecordState` (unwrap) SMT. Raw topics keep
  the full Debezium envelope; clean topics are produced by the engine normalizer.
- CDR and Payment use **separate** replication slots, publications, and signaling
  tables.

---

## Demo Scenarios

Triggered from the dashboard buttons (or the engine REST API):

- Start CDR Normal Flow
- Start CDR Suspicious Movement
- Start CDR Impossible Signal
- Start CDR Split Signal
- Start Payment Normal Flow
- Start Payment Impossible Transaction
- Start Duplicate Payment
- Start Velocity Fraud
- Start 3DS Challenge Scenario
- Run CDR Historical Backfill
- Run Payment Historical Backfill
- Reset Demo

---

## CDR Mode

Detects physically impossible subscriber mobility in a synthetic German telecom
signal stream. See [docs/cdr-mode.md](docs/cdr-mode.md).

## Payment Mode

Detects impossible card travel, duplicate payments, velocity fraud, geo mismatch,
and 3DS decisions in a synthetic payment stream. See
[docs/payment-fraud-mode.md](docs/payment-fraud-mode.md).

## Incremental Snapshot Demo

Replays historical synthetic records using Debezium incremental snapshots as a bonus
demo scene. See [docs/incremental-snapshot.md](docs/incremental-snapshot.md).

---

## Observability

The engine exposes metrics via Spring Boot Actuator:

- `/actuator/health`
- `/actuator/prometheus`
- `/actuator/metrics`

Prometheus scrapes the engine and Grafana provides CDR and Payment fraud dashboards.

---

## Production Vision

Kubernetes, Strimzi, Helm, a Flink / Spark Structured Streaming alternative, a
ClickHouse / Elasticsearch analytical sink, OpenTelemetry, an optional Redis Phase 2
cache, and an optional LLM-based RCA/explanation layer. See
[docs/production-vision.md](docs/production-vision.md).

---

## Safety / Synthetic Data Disclaimer

- Never use real card data.
- Never store PAN, CVV, PIN, or sensitive authentication data.
- Use only synthetic tokenized payment records (`card_token`, `masked_pan`, `last4`).
- This is a fraud detection **simulation**, not a real payment processor.
- All CDR data is synthetic. No real subscriber or personal data.

---

_ExploratorX — Author: Melih Ayçiçek · Version 1.1 · June 2026_
