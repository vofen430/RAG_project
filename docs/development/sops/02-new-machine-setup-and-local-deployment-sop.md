# New Machine Setup and Local Deployment SOP

## 1. Purpose

This SOP defines the exact steps and commands to set up and deploy the Novel Character RAG system on a new local machine. All commands are tested and verified as of 2026-03-12.

## 2. Prerequisites

### 2.1 Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| Git | 2.x+ | Version control |
| Docker Engine | 20.x+ | Container runtime for PostgreSQL and Redis |
| Docker Compose | v2+ | Multi-container orchestration |
| Java (JDK) | 17 | Backend runtime (Spring Boot 3.2) |
| Node.js | 20+ (LTS) | Frontend build and dev server |
| npm | 9+ | Frontend package manager |
| Maven Wrapper | Bundled | Backend build (included in repo as `./mvnw`) |

### 2.2 Verify Installed Versions

```bash
git --version          # git version 2.x.x
docker --version       # Docker version 20+
docker compose version # Docker Compose version v2.x.x
java -version          # openjdk version "17.x.x"
node -v                # v20.x.x or higher
npm -v                 # 9.x.x or higher
```

## 3. Project Checkout

```bash
# 1. Clone the repository
git clone https://github.com/vofen430/RAG_project.git
cd RAG_project

# 2. Verify repository structure
ls -la backend/ frontend/ docker-compose.yml docs/
```

## 4. Infrastructure Services

### 4.1 Start PostgreSQL (with pgvector) and Redis

```bash
# Start both services in background
docker compose up -d

# Verify services are healthy
docker compose ps
# Expected output:
#   rag-postgres   running (healthy)
#   rag-redis      running (healthy)
```

### 4.2 Verify PostgreSQL

```bash
docker exec rag-postgres psql -U rag_user -d rag_local -c "SELECT 1;"
# Expected: 1 row returned

# Verify pgvector extension is available
docker exec rag-postgres psql -U rag_user -d rag_local -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

### 4.3 Verify Redis

```bash
docker exec rag-redis redis-cli ping
# Expected: PONG
```

## 5. SiliconFlow API Key

Obtain a SiliconFlow API key:

1. Visit https://cloud.siliconflow.com/account/ak
2. Register/login and create an API Key (format: `sk-xxxx...xxxx`)
3. Keep it ready for the next step

## 6. Backend Setup

### 6.1 Environment Variables

Set the following environment variables before starting the backend:

```bash
# Required
export MODEL_PROVIDER_API_KEY="sk-your-siliconflow-api-key"
export JWT_SECRET="replace-with-local-secret-key-at-least-32-chars-long"

# Optional (defaults shown, override as needed)
export SERVER_PORT=8080
export DB_HOST=127.0.0.1
export DB_PORT=5432
export DB_NAME=rag_local
export DB_USERNAME=rag_user
export DB_PASSWORD=rag_password
export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
export LOCAL_STORAGE_ROOT=./storage
```

### 6.2 Start the Backend

```bash
cd backend

# Download dependencies and compile (first time takes ~2 minutes)
./mvnw clean compile -q

# Start the application
./mvnw spring-boot:run
```

### 6.3 Verify Backend Startup

Look for the following log output:

```
INFO  o.f.core.internal.command.DbMigrate : Migrating schema "public" to version "1 - init schema"
INFO  o.f.core.internal.command.DbMigrate : Migrating schema "public" to version "2 - seed default user"
INFO  o.f.core.internal.command.DbMigrate : Migrating schema "public" to version "3 - add api key to settings"
INFO  com.rag.NovelRagApplication : Started NovelRagApplication in X.XXX seconds
```

### 6.4 Verify Backend Health

```bash
curl -s http://localhost:8080/actuator/health | python3 -m json.tool
# Expected: {"status":"UP","components":{...}}
```

## 7. Frontend Setup

### 7.1 Install Dependencies

```bash
cd frontend
npm install
```

### 7.2 Start Development Server

```bash
npm run dev
# Or with host binding for network access:
npx vite --host 0.0.0.0 --port 5173
```

### 7.3 Verify Frontend

Open http://localhost:5173 in a browser. You should see the login page.

## 8. Deployment Verification Checklist

After both backend and frontend are running, verify the following:

### 8.1 Login

```bash
# Default demo account (seeded by migration V2)
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo-password"}' | python3 -m json.tool
# Expected: {"code":"OK","data":{"accessToken":"eyJ...","refreshToken":"..."}}
```

### 8.2 Settings API

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo-password"}' | \
  python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

curl -s http://localhost:8080/api/settings \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
# Expected: settings with hasApiKey, model names, RAG parameters
```

### 8.3 Model Options API

```bash
curl -s http://localhost:8080/api/settings/models \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
# Expected: embedding/reranking/chat model options from SiliconFlow catalog
```

## 9. Default Configuration

| Parameter | Default Value | Source |
|-----------|--------------|--------|
| Server Port | 8080 | `application.yml` |
| PostgreSQL | 127.0.0.1:5432/rag_local | docker-compose |
| Redis | 127.0.0.1:6379 | docker-compose |
| Frontend | http://localhost:5173 | Vite dev server |
| Embedding Model | Qwen/Qwen3-Embedding-0.6B | `models.json` |
| Rerank Model | Qwen/Qwen3-Reranker-0.6B | `models.json` |
| Chat Model | Qwen/Qwen3-8B | `models.json` |
| Chunk Size | 500 chars | `application.yml` |
| Chunk Overlap | 100 chars | `application.yml` |
| Top-K Retrieval | 10 | `application.yml` |
| Top-N Rerank | 5 | `application.yml` |
| Demo Account | demo / demo-password | V2 seed migration |

## 10. Directory Structure

```
RAG_project/
├── backend/                  # Spring Boot 3.2 + MyBatis-Plus
│   ├── src/main/java/com/rag/
│   │   ├── config/           # SiliconFlowConfig, ModelConfig, SecurityConfig
│   │   ├── controller/       # REST endpoints
│   │   ├── entity/           # MyBatis-Plus entities
│   │   ├── mapper/           # Database mappers
│   │   ├── security/         # JWT auth, filters
│   │   └── service/          # Business logic (RAG pipeline)
│   └── src/main/resources/
│       ├── application.yml   # Configuration
│       ├── models.json       # SiliconFlow model catalog
│       └── db/migration/     # Flyway SQL migrations (V1-V3)
├── frontend/                 # Vue.js 3 + Vite
│   └── src/
│       ├── api/index.js      # Axios API client
│       ├── views/            # Page components
│       └── style.css         # Global styles
├── storage/                  # Document file storage (auto-created)
│   └── documents/{userId}/   # Uploaded files
├── docker-compose.yml        # PostgreSQL + Redis
├── siliconflow/              # API documentation
└── docs/                     # Project documentation
```

## 11. Shutdown

```bash
# 1. Stop frontend (Ctrl+C or kill the Vite process)
# 2. Stop backend (Ctrl+C or kill the Spring Boot process)
# 3. Stop infrastructure services
docker compose down

# To also remove data volumes (DESTRUCTIVE):
# docker compose down -v
```

## 12. Troubleshooting

| Issue | Solution |
|-------|---------|
| Port 5432 in use | `docker compose down` then `docker compose up -d` |
| Port 8080 in use | Set `SERVER_PORT=8081` (or kill the conflicting process) |
| `pgvector` extension not found | The `pgvector/pgvector:pg16` image includes it by default |
| Flyway migration fails | Check DB connection; ensure `rag_local` database exists |
| API key error (401) | Set `MODEL_PROVIDER_API_KEY` env var or configure via Settings UI |
| File upload fails | Check `LOCAL_STORAGE_ROOT` permissions |
| GBK encoding garbled | Backend auto-detects GBK; ensure file is not double-converted |
