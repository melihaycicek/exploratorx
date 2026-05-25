#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────────────────
# ExploratorX — CDR Kafka topics
#
# Creates the clean and decision topics for CDR mode. Raw Debezium topics
# (exploratorx.cdr.public.cdr_signal) are created automatically by the connector.
#
# Keyed by subscriber_id so the same subscriber always lands on the same
# partition -> correct stateful comparison in Kafka Streams / RocksDB.
#
# Usage (from the host):
#   docker exec -it exploratorx-kafka bash /path/to/cdr-topics.sh
# or with KAFKA_BOOTSTRAP pointing at the broker:
#   KAFKA_BOOTSTRAP=localhost:29092 ./cdr-topics.sh
# ──────────────────────────────────────────────────────────────────────────────
set -euo pipefail

KAFKA_BOOTSTRAP="${KAFKA_BOOTSTRAP:-exploratorx-kafka:9092}"
PARTITIONS="${PARTITIONS:-3}"
REPLICATION="${REPLICATION:-1}"

create_topic() {
  local topic="$1"
  echo "Creating topic: ${topic} (partitions=${PARTITIONS}, rf=${REPLICATION})"
  kafka-topics --bootstrap-server "${KAFKA_BOOTSTRAP}" \
    --create --if-not-exists \
    --topic "${topic}" \
    --partitions "${PARTITIONS}" \
    --replication-factor "${REPLICATION}"
}

# Clean topic produced by the engine normalizer/topology
create_topic "exploratorx.cdr.signals.clean"

# Decision topic produced by the CDR anomaly engine
create_topic "exploratorx.cdr.anomalies"

echo "CDR topics ready."
