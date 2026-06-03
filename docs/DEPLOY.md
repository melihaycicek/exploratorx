# ExploratorX — Production Deployment Guide

> **Hedef:** `https://demo.audfix.com/exploratorx`
> **Auth:** Audfix kendi DB'si üzerinden yönetir — Nginx `auth_request` ile kontrol eder.

---

## Sunucu Gereksinimleri

| Kaynak | Minimum | Önerilen |
|--------|---------|----------|
| CPU | 4 vCPU | 8 vCPU |
| RAM | 8 GB | 16 GB |
| Disk | 40 GB SSD | 80 GB SSD |
| OS | Ubuntu 22.04 LTS | Ubuntu 22.04 LTS |
| Docker | 25.x | 25.x |
| Docker Compose | v2.24+ | v2.24+ |

> [!IMPORTANT]
> Kafka + Debezium + Kafka Connect birlikte ~3 GB RAM tutar.
> PostgreSQL WAL retention için disk alanına dikkat edin.

---

## 1. Sunucu Hazırlığı

```bash
# Sistem güncelleme
sudo apt-get update && sudo apt-get upgrade -y

# Docker kurulumu
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Docker Compose v2
sudo apt-get install docker-compose-plugin -y

# Doğrulama
docker version && docker compose version
```

---

## 2. Repository Clone

```bash
git clone https://github.com/melihaycicek/exploratorx.git /opt/exploratorx
cd /opt/exploratorx
```

---

## 3. Environment Dosyası

```bash
cp .env.example .env.prod
nano .env.prod
```

`.env.prod` içinde mutlaka değiştirilmesi gerekenler:

```env
# PostgreSQL
POSTGRES_PASSWORD=<güçlü_rastgele_şifre>

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# Grafana
GRAFANA_ADMIN_PASSWORD=<güçlü_şifre>

# Engine
SERVER_PORT=8080

# Dashboard
NEXT_PUBLIC_ENGINE_URL=https://demo.audfix.com/exploratorx/api

# Security
EXPLORATORX_JWT_SECRET=<en_az_32_karakter_rastgele>
```

> [!CAUTION]
> `.env.prod` dosyasını asla Git'e eklemeyin. `.gitignore`'da zaten kayıtlı.

---

## 4. Nginx Konfigürasyonu (audfix.com sunucusunda)

Audfix sunucusundaki Nginx config'e `/exploratorx` location bloğunu ekleyin:

```nginx
# /etc/nginx/sites-available/demo.audfix.com
# (mevcut /audroad bloğunun altına ekleyin)

location /exploratorx/api/ {
    # Audfix auth kontrolü
    auth_request /audfix/auth/verify;
    auth_request_set $auth_status $upstream_status;

    proxy_pass http://127.0.0.1:8080/api/;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

location /exploratorx/api/ws {
    # WebSocket için auth
    auth_request /audfix/auth/verify;

    proxy_pass http://127.0.0.1:8080/ws;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
    proxy_read_timeout 3600s;
}

location /exploratorx/ {
    # Audfix auth kontrolü
    auth_request /audfix/auth/verify;
    auth_request_set $auth_status $upstream_status;

    # 401 → Audfix login sayfasına yönlendir
    error_page 401 = @exploratorx_login_redirect;

    proxy_pass http://127.0.0.1:3000/;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}

location @exploratorx_login_redirect {
    return 302 https://audfix.com/login?next=/exploratorx/;
}
```

```bash
# Nginx config test ve reload
sudo nginx -t && sudo systemctl reload nginx
```

---

## 5. İlk Başlatma

### 5a. Core servisler

```bash
cd /opt/exploratorx

# Core (PostgreSQL + Kafka + Debezium + Engine + Dashboard)
docker compose --env-file .env.prod up -d

# Servis durumu kontrol
docker compose ps
```

### 5b. Sağlık kontrolü — sırayla bekleyin

```bash
# 1. PostgreSQL hazır mı?
docker compose exec postgres pg_isready

# 2. Kafka hazır mı? (30-60 saniye bekleyin)
docker compose exec kafka kafka-topics.sh --bootstrap-server localhost:29092 --list

# 3. Engine ayağa kalktı mı?
curl http://localhost:8080/api/health

# 4. Dashboard çalışıyor mu?
curl http://localhost:3000
```

### 5c. Debezium Connector Kayıt

**CDR Connector:**

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @infra/debezium/connectors/cdr-connector.json
```

**Payment Connector:**

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @infra/debezium/connectors/payment-connector.json
```

**Connector durumu kontrol:**

```bash
curl http://localhost:8083/connectors/cdr-connector/status | python3 -m json.tool
curl http://localhost:8083/connectors/payment-connector/status | python3 -m json.tool
```

Her iki connector'ın da `state: RUNNING` göstermesi gerekir.

### 5d. Observability (opsiyonel ama önerilen)

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml \
  --env-file .env.prod up -d

# Grafana: http://localhost:3001 (Nginx arkasında /grafana olabilir)
# Prometheus: http://localhost:9090
```

---

## 6. İlk Veri Testi

```bash
# CDR normal scenario tetikle
curl -X POST http://localhost:8080/api/demo/cdr/normal

# 5 saniye bekle, anomaly kontrol
curl http://localhost:8080/api/anomalies | python3 -m json.tool

# Payment impossible scenario
curl -X POST http://localhost:8080/api/demo/pay/impossible
curl http://localhost:8080/api/stats | python3 -m json.tool
```

Dashboard `https://demo.audfix.com/exploratorx/` üzerinden canlı akışı izleyin.

---

## 7. Kafka Topic Yönetimi

Topicler `docker-compose.yml` içindeki `kafka-init` servisi tarafından otomatik oluşturulur. Manuel kontrol için:

```bash
# Topic listesi
docker compose exec kafka kafka-topics.sh \
  --bootstrap-server localhost:29092 --list

# CDR topic detayı
docker compose exec kafka kafka-topics.sh \
  --bootstrap-server localhost:29092 \
  --describe --topic exploratorx.cdr.signals.clean
```

---

## 8. Güncelleme Prosedürü

```bash
cd /opt/exploratorx

# Yeni kodları çek
git pull origin master

# Engine rebuild (zero-downtime değil, kısa kesinti)
docker compose --env-file .env.prod up -d --build exploratorx-engine

# Dashboard rebuild
docker compose --env-file .env.prod up -d --build exploratorx-dashboard

# Debezium connector'ları kontrol (genellikle etkilenmez)
curl http://localhost:8083/connectors/cdr-connector/status
```

> [!WARNING]
> Kafka Streams RocksDB state dizini (`/tmp/kafka-streams/exploratorx`)
> container restart'ta temizlenir. State kaybını önlemek için
> `application.yml`'de `state-dir` volume'a mount edilmiş bir yola taşıyın.

---

## 9. Yedekleme

### PostgreSQL

```bash
# Günlük dump
docker compose exec postgres pg_dump \
  -U exploratorx exploratorx | gzip > /backup/pg_$(date +%Y%m%d).sql.gz
```

### Kafka

Kafka verisi geçici — Debezium WAL'dan yeniden üretebilir. Önemli olan PostgreSQL yedekleridir.

### Grafana

```bash
docker cp exploratorx-grafana:/var/lib/grafana /backup/grafana_$(date +%Y%m%d)
```

---

## 10. Sorun Giderme

| Belirti | Kontrol |
|---------|---------|
| Dashboard bağlanamıyor | `docker compose logs exploratorx-dashboard` |
| WebSocket bağlantısı kesilir | `docker compose logs exploratorx-engine` |
| Anomali gelmiyor | Debezium connector durumu: `curl localhost:8083/connectors/cdr-connector/status` |
| Kafka Streams REBALANCING | Engine restart: `docker compose restart exploratorx-engine` |
| Grafana panel boş | Prometheus target'ı kontrol: `http://localhost:9090/targets` |
| Engine START_UP aşamasında kalıyor | PostgreSQL bağlantısı: `docker compose logs postgres` |

---

## 11. Port Haritası (dahili)

| Servis | Port |
|--------|------|
| PostgreSQL | 5432 |
| Kafka | 29092 |
| Kafka Connect (Debezium) | 8083 |
| Kafka UI | 8989 |
| ExploratorX Engine | 8080 |
| ExploratorX Dashboard | 3000 |
| Prometheus | 9090 |
| Grafana | 3001 |

> Nginx dışa açık portlar: **443 (HTTPS)** ve **80 (HTTP → 443 redirect)**
> Diğer tüm portlar yalnızca localhost'a bind olmalıdır (UFW/güvenlik duvarı ile koru).

---

*Son güncelleme: 3 Haziran 2026 — Claude Sonnet 4.6*
