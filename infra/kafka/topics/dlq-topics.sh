#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────────────────
# ExploratorX — Common topics (audit + dead-letter queue)
#
# Shared across CDR and Payment modes.
#
# Usage (from the host):
#   docker exec -it exploratorx-kafka bash /path/to/dlq-topics.sh
# or:
#   KAFKA_BOOTSTRAP=localhost:29092 ./dlq-topics.sh
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

# Audit trail of all processed events/decisions
create_topic "exploratorx.audit"

# Dead-letter queue for poison/unparseable records
create_topic "exploratorx.dlq"

echo "Common topics ready."
