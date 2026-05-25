# ExploratorX — Production Vision

> Skeleton document. Forward-looking; not part of the MVP.

The MVP is intentionally a single-engine, Docker Compose deployment. A production
evolution could include:

- **Kubernetes** — orchestration and scaling.
- **Strimzi** — Kafka on Kubernetes.
- **Helm** — packaging and deployment.
- **Flink or Spark Structured Streaming** — alternative/large-scale stream processing.
- **ClickHouse / Elasticsearch** — analytical sink for historical queries.
- **OpenTelemetry** — distributed tracing and unified telemetry.
- **Redis (optional, Phase 2)** — shared/low-latency cache (not used in MVP).
- **LLM (optional)** — RCA / explanation layer for detected anomalies.

## Out of MVP scope

- Redis
- LLM integration
- Multi-broker Kafka, HA Postgres, external secrets management

## TODO

- Reference deployment topology
- Capacity and partitioning guidance
