# New Machine Setup and Local Deployment SOP

## 1. Purpose

This SOP defines the standard steps for preparing a new local machine and deploying the RAG project for local use.

## 2. Required Software

Install the following software on the new machine:

- Git
- Docker Engine
- Docker Compose v2
- Java 17
- Node.js 24
- npm 11

## 3. Required Local Services

The new machine must run:

- PostgreSQL 16 with pgvector
- Redis 7

## 4. Project Checkout SOP

Steps:

1. Create a local workspace directory.
2. Clone the project repository into the workspace directory.
3. Switch to the required branch.
4. Confirm the repository is clean before deployment work starts.

## 5. Local Directory SOP

Create the following directories on the local machine:

- Project workspace directory
- Local document storage root
- Local log directory
- Local temporary directory

## 6. Environment File SOP

Prepare local environment configuration with the following values:

- Backend port
- PostgreSQL host
- PostgreSQL port
- PostgreSQL database name
- PostgreSQL username
- PostgreSQL password
- Redis host
- Redis port
- JWT secret
- Local storage root
- Model provider API key
- Frontend API base URL

## 7. PostgreSQL Initialization SOP

Steps:

1. Start PostgreSQL locally.
2. Create the `rag_local` database.
3. Create the application database user.
4. Grant the required database privileges.
5. Enable the `vector` extension in `rag_local`.

## 8. Redis Initialization SOP

Steps:

1. Start Redis locally.
2. Verify Redis is reachable on the configured port.

## 9. Backend Setup SOP

Steps:

1. Go to the `backend` directory.
2. Confirm Java 17 is active.
3. Confirm backend environment variables are configured.
4. Run Maven dependency resolution.
5. Start the backend application.
6. Verify that database migration completes successfully.
7. Verify that the backend health endpoint returns success.

## 10. Frontend Setup SOP

Steps:

1. Go to the `frontend` directory.
2. Confirm Node.js 24 and npm 11 are active.
3. Install frontend dependencies.
4. Configure the frontend local environment.
5. Start the frontend.
6. Open the local frontend URL.

## 11. Local Deployment Verification SOP

After startup, verify:

1. Login page loads.
2. User login succeeds.
3. Document upload succeeds.
4. Indexing starts successfully.
5. Indexing completes successfully.
6. Chat streaming works.
7. Trace detail can be opened.
8. Citation evidence can be opened.

## 12. Shutdown SOP

Stop local services in the following order:

1. Frontend process
2. Backend process
3. Redis
4. PostgreSQL

## 13. Handoff Record SOP

Record the following after deployment:

- Machine name
- Deployment date
- Branch name
- Backend startup result
- Frontend startup result
- PostgreSQL check result
- Redis check result
- Functional verification result
