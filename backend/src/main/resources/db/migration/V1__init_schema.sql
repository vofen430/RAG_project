-- V1: Enable pgvector extension and create core tables
-- RAG Local Database Schema

-- Enable pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- 1. users
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(64) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    role_code VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2. user_settings
CREATE TABLE user_settings (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    embedding_model VARCHAR(128) NOT NULL,
    rerank_model VARCHAR(128) NOT NULL,
    chat_model VARCHAR(128) NOT NULL,
    chunk_size INTEGER NOT NULL DEFAULT 500,
    chunk_overlap INTEGER NOT NULL DEFAULT 100,
    top_k INTEGER NOT NULL DEFAULT 10,
    top_n INTEGER NOT NULL DEFAULT 5,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 3. documents
CREATE TABLE documents (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_ext VARCHAR(16) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    file_hash VARCHAR(128) NOT NULL,
    storage_path VARCHAR(512) NOT NULL,
    source_encoding VARCHAR(32) NOT NULL,
    document_status VARCHAR(32) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_documents_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 4. indexing_jobs
CREATE TABLE indexing_jobs (
    id VARCHAR(36) PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL,
    job_status VARCHAR(32) NOT NULL,
    current_stage VARCHAR(64) NOT NULL,
    processed_chunks INTEGER NOT NULL DEFAULT 0,
    total_chunks INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_indexing_jobs_document FOREIGN KEY (document_id) REFERENCES documents(id)
);

-- 5. document_chunks
CREATE TABLE document_chunks (
    id VARCHAR(36) PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL,
    chunk_index INTEGER NOT NULL,
    section_label VARCHAR(255),
    content_text TEXT NOT NULL,
    content_summary TEXT,
    start_offset INTEGER NOT NULL,
    end_offset INTEGER NOT NULL,
    entity_list JSONB NOT NULL DEFAULT '[]'::jsonb,
    embedding_model VARCHAR(128) NOT NULL,
    embedding_vector vector(1024) NOT NULL,
    content_hash VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_document_chunks_document FOREIGN KEY (document_id) REFERENCES documents(id)
);

CREATE INDEX idx_document_chunks_document_id ON document_chunks(document_id);
CREATE UNIQUE INDEX idx_document_chunks_doc_chunk ON document_chunks(document_id, chunk_index);

-- 6. chat_sessions
CREATE TABLE chat_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    session_status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_chat_sessions_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 7. chat_messages
CREATE TABLE chat_messages (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    message_role VARCHAR(32) NOT NULL,
    message_index INTEGER NOT NULL,
    content_text TEXT NOT NULL,
    trace_id VARCHAR(36),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id)
);

CREATE UNIQUE INDEX idx_chat_messages_session_index ON chat_messages(session_id, message_index);

-- 8. rag_query_traces
CREATE TABLE rag_query_traces (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    user_query TEXT NOT NULL,
    rewritten_query TEXT,
    retrieval_top_k INTEGER NOT NULL,
    rerank_top_n INTEGER NOT NULL,
    prompt_version VARCHAR(64) NOT NULL,
    answer_status VARCHAR(32) NOT NULL,
    answer_text TEXT,
    latency_ms INTEGER,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_rag_query_traces_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id),
    CONSTRAINT fk_rag_query_traces_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 9. trace_evidence_items
CREATE TABLE trace_evidence_items (
    id VARCHAR(36) PRIMARY KEY,
    trace_id VARCHAR(36) NOT NULL,
    document_id VARCHAR(36) NOT NULL,
    chunk_id VARCHAR(36) NOT NULL,
    retrieval_rank INTEGER NOT NULL,
    retrieval_score NUMERIC(12,6) NOT NULL,
    rerank_rank INTEGER,
    rerank_score NUMERIC(12,6),
    is_selected BOOLEAN NOT NULL,
    citation_no INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_trace_evidence_trace FOREIGN KEY (trace_id) REFERENCES rag_query_traces(id),
    CONSTRAINT fk_trace_evidence_document FOREIGN KEY (document_id) REFERENCES documents(id),
    CONSTRAINT fk_trace_evidence_chunk FOREIGN KEY (chunk_id) REFERENCES document_chunks(id)
);

CREATE INDEX idx_trace_evidence_trace_id ON trace_evidence_items(trace_id);
CREATE INDEX idx_trace_evidence_trace_citation ON trace_evidence_items(trace_id, citation_no);

-- 10. document_feedback
CREATE TABLE document_feedback (
    id VARCHAR(36) PRIMARY KEY,
    trace_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    feedback_type VARCHAR(32) NOT NULL,
    feedback_text TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_document_feedback_trace FOREIGN KEY (trace_id) REFERENCES rag_query_traces(id),
    CONSTRAINT fk_document_feedback_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create IVFFlat index on embedding vectors for similarity search
-- Note: IVFFlat index requires the table to have some data before creating.
-- This index will be created after initial data is loaded.
-- CREATE INDEX idx_document_chunks_embedding ON document_chunks USING ivfflat (embedding_vector vector_cosine_ops) WITH (lists = 100);
