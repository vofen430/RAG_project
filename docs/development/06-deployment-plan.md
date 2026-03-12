# Local Deployment Plan

## 1. Deployment Scope

This project is deployed on a local machine only.

The deployment includes:

- Frontend application
- Backend application
- PostgreSQL
- Redis
- Local storage directory

## 2. Runtime Ports

Reserved local ports:

- Frontend: `5173`
- Backend: `8080`
- PostgreSQL: `5432`
- Redis: `6379`

## 3. Required Local Directories

The local machine must provide the following directories:

- Project root directory
- Local document storage directory
- Local application log directory
- Local PostgreSQL data directory when container volume mapping is required
- Local Redis data directory when container volume mapping is required

## 4. Required Environment Variables

Backend environment variables:

- `SPRING_PROFILES_ACTIVE=local`
- `SERVER_PORT=8080`
- `DB_HOST=127.0.0.1`
- `DB_PORT=5432`
- `DB_NAME=rag_local`
- `DB_USERNAME=rag_user`
- `DB_PASSWORD=rag_password`
- `REDIS_HOST=127.0.0.1`
- `REDIS_PORT=6379`
- `JWT_SECRET=replace-with-local-secret`
- `LOCAL_STORAGE_ROOT=/absolute/path/to/storage`
- `MODEL_PROVIDER_API_KEY=replace-with-local-key`

Frontend environment variables:

- `VITE_API_BASE_URL=http://127.0.0.1:8080/api`

## 5. Local Service Layout

### 5.1 Backend Process

The backend runs as one Spring Boot process and connects to:

- PostgreSQL on localhost
- Redis on localhost
- Local storage path on the local file system

### 5.2 Frontend Process

The frontend runs as one Vite process during development or as one local static process during local release verification.

### 5.3 Database Process

PostgreSQL runs locally with pgvector enabled.

### 5.4 Cache Process

Redis runs locally as a single instance.

## 6. Local Data Layout

The local storage root should contain:

- `/documents`
- `/tmp`
- `/exports`
- `/logs`

## 7. Startup Order

The required startup order is:

1. PostgreSQL
2. Redis
3. Backend
4. Frontend

## 8. Migration Order

The required migration order is:

1. Start PostgreSQL
2. Enable pgvector
3. Start backend
4. Run Flyway migrations automatically on backend startup

## 9. Operational Checks

After local deployment, verify:

- PostgreSQL is reachable
- Redis is reachable
- Backend health endpoint is up
- Frontend homepage is reachable
- Login works
- Document upload works
- Indexing works
- Chat streaming works
- Trace detail loading works

## 10. Local Backup Scope

The local backup scope includes:

- PostgreSQL data
- Local source documents
- Local log directory when required for troubleshooting

## 11. Local Stop Order

The required stop order is:

1. Frontend
2. Backend
3. Redis
4. PostgreSQL
