#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────────────────
# ExploratorX — Payment Kafka topics
#
# Creates the clean and fraud-alert topics for Payment mode. Raw Debezium topics
# (exploratorx.pay.public.payment_transaction) are created by the connector.
#
# Keyed by card_token so the same card always lands on the same partition ->
# correct stateful comparison in Kafka Streams / RocksDB.
#
# Usage (from the host):
#   docker exec -it exploratorx-kafka bash /path/to/payment-topics.sh
# or:
#   KAFKA_BOOTSTRAP=localhost:29092 ./payment-topics.sh
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
create_topic "exploratorx.pay.transactions.clean"

# Decision topic produced by the payment fraud engine
create_topic "exploratorx.pay.fraud.alerts"

echo "Payment topics ready."
