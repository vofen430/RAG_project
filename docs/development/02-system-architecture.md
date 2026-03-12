# System Architecture Design Specification

## 1. Overview

The system is a locally deployed frontend and backend application with one relational database, one in-memory cache service, and one local document storage directory.

The system consists of:

- A Vue 3 frontend for document management, chat interaction, trace review, and settings management
- A Spring Boot backend for authentication, document ingestion, indexing, retrieval, answer generation, and trace persistence
- PostgreSQL with pgvector for business data and vector search
- Redis for cache, session-related data, short-lived task state, and request throttling data
- A local file storage directory for uploaded source files

## 2. Runtime Components

### 2.1 Frontend

The frontend is responsible for:

- User login and session bootstrap
- Document upload and status display
- Chat session creation and message display
- Streaming answer rendering
- Citation navigation
- Evidence panel rendering
- Retrieval trace display
- User settings operations

### 2.2 Backend

The backend is responsible for:

- Authentication and authorization
- File upload handling
- Document metadata persistence
- Chunk generation
- Embedding generation orchestration
- Vector persistence
- Similarity retrieval
- Reranking orchestration
- Prompt assembly
- Streaming answer output
- Citation generation
- Query trace persistence
- Job status management

### 2.3 PostgreSQL

PostgreSQL is the system of record for:

- User accounts
- Document metadata
- Document chunks
- Vector embeddings
- Indexing jobs
- Chat sessions
- Chat messages
- Query traces
- Citation records
- User settings

### 2.4 Redis

Redis is used for:

- Access token and refresh token support data
- Login-related short-lived session data
- Request throttling counters
- Cache for repeated read operations
- Short-lived indexing progress cache

### 2.5 Local File Storage

Local file storage is used for:

- Uploaded source files
- Temporary import files
- Optional export artifacts

## 3. Functional Flow

### 3.1 Document Ingestion Flow

The document ingestion flow is:

1. The frontend uploads a file.
2. The backend validates the request and stores the source file on local disk.
3. The backend creates a document record in PostgreSQL.
4. The backend creates an indexing job record.
5. The backend starts the indexing pipeline.
6. The backend writes chunk records and vector data to PostgreSQL.
7. The backend updates indexing job status until completion.

### 3.2 Query Flow

The query flow is:

1. The frontend sends a user query within a chat session.
2. The backend creates a query trace record.
3. The backend rewrites the query when required.
4. The backend retrieves candidate chunks from PostgreSQL with pgvector.
5. The backend reranks the candidate chunks.
6. The backend builds the final prompt input.
7. The backend calls the model provider integration layer.
8. The backend streams the answer back to the frontend.
9. The backend stores the final answer, citations, and retrieval trace.

### 3.3 Explainability Flow

The explainability flow is:

1. Retrieved chunks receive a stable citation number.
2. Each citation is mapped to document, chunk, and location metadata.
3. The final answer includes citation markers.
4. The frontend loads the evidence list and trace details for the answer.
5. The user can review evidence text and retrieval metadata.

## 4. Module Boundaries

### 4.1 Frontend Modules

- Authentication
- Document management
- Indexing status
- Chat
- Evidence and trace review
- Settings

### 4.2 Backend Modules

- Auth module
- User settings module
- Document module
- Indexing module
- Retrieval module
- Reranking module
- Prompt module
- Chat module
- Trace module
- Health module

## 5. Persistence Boundary

The persistence boundary is:

- Source files are stored on local disk
- Structured application data is stored in PostgreSQL
- Short-lived cache and counters are stored in Redis

## 6. Security Boundary

The security boundary is:

- All application endpoints are authenticated except login and health
- Document data is isolated by user ownership
- Chat sessions are isolated by user ownership
- Trace records are isolated by user ownership
- Provider keys and sensitive application secrets are stored outside source code

## 7. Local Deployment Boundary

The local deployment boundary is:

- One local machine
- One application backend process
- One application frontend process
- One PostgreSQL instance
- One Redis instance
- One local storage path

## 8. Logging Boundary

The backend must log:

- Authentication events
- File upload events
- Indexing lifecycle events
- Query lifecycle events
- Retrieval result counts
- Rerank result counts
- Error events

The frontend must log:

- Login failure
- Upload failure
- Streaming interruption
- Trace loading failure
