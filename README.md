# Intelligent Logistics Control Tower

Phase 1 implementation scaffold for:
- `logistics-core` (Java Spring Boot)
- `ai-assistant` (Python FastAPI)
- PostgreSQL + Flyway migrations via Docker Compose

## Repository Layout

- `services/logistics-core` - Core orders API (Spring Boot + JPA)
- `services/ai-assistant` - AI assistant API for shipment exception summaries (FastAPI)
- `database/migrations` - Flyway SQL migration scripts
- `docker-compose.yml` - Local PostgreSQL + Flyway orchestration

## Prerequisites

- Docker Desktop
- Python 3.10+
- Java 21
- Maven 3.9+ (or configure Maven in your IDE)

## 1) Start Database and Migrations

```bash
docker compose --env-file .env.example up -d postgres
docker compose --env-file .env.example run --rm flyway
```

## 2) Run AI Assistant Service

```bash
python -m venv .venv
.venv/Scripts/python -m pip install -r services/ai-assistant/requirements.txt
cd services/ai-assistant
../../.venv/Scripts/python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Health check:

```bash
curl http://localhost:8000/health
```

## 3) Run Java Core Service

```bash
mvn -f services/logistics-core/pom.xml spring-boot:run
```

Health check:

```bash
curl http://localhost:8080/actuator/health
```

## Current Implemented Endpoints

### Core Service

- `POST /api/orders`
- `GET /api/orders`

Sample request:

```json
{
  "customerName": "ACME Corp"
}
```

### AI Assistant

- `GET /health`
- `POST /api/assistant/summarize`

Sample request:

```json
{
  "shipment_id": "SHIP-101",
  "exception_type": "DELAYED",
  "latest_status": "IN_TRANSIT",
  "operator_notes": "Weather issue"
}
```

## Tests

AI assistant:

```bash
cd services/ai-assistant
../../.venv/Scripts/python -m pytest tests -q
```

Core service:

```bash
mvn -f services/logistics-core/pom.xml test
```

## Next Implementation Steps

## Incremental PR Roadmap

All implementation proceeds as one concern per PR.

1. PR-00: Monorepo and developer infrastructure baseline.
2. PR-01: Database schema and Flyway migration for orders/shipments.
3. PR-02: Core service order domain + create/list API.
4. PR-03: Shipment domain and shipment CRUD API.
5. PR-04: Exception workflow API + state transitions.
6. PR-05: AI assistant summarization endpoint integration contract.
7. PR-06: Integration tests (core + PostgreSQL) and fixtures.
8. PR-07: OpenAPI docs and API usage examples.
9. PR-08: CI pipeline (lint, tests, migration checks).

Each PR should include:

- A clear single-scope statement
- Tests relevant to that scope
- Validation commands and outcomes
- Out-of-scope section to prevent concern mixing
