# Database Design Document

## 1. Database Platform

- PostgreSQL 16
- pgvector extension enabled

## 2. Database Naming

- Database name: `rag_local`
- Schema name: `public`

## 3. Core Tables

### 3.1 `users`

Purpose:

- Store local application user accounts

Fields:

- `id` UUID primary key
- `username` varchar(64) unique not null
- `password_hash` varchar(255) not null
- `display_name` varchar(128) not null
- `role_code` varchar(32) not null
- `status` varchar(32) not null
- `created_at` timestamptz not null
- `updated_at` timestamptz not null

### 3.2 `user_settings`

Purpose:

- Store per-user application settings

Fields:

- `id` UUID primary key
- `user_id` UUID not null
- `embedding_model` varchar(128) not null
- `rerank_model` varchar(128) not null
- `chat_model` varchar(128) not null
- `chunk_size` integer not null
- `chunk_overlap` integer not null
- `top_k` integer not null
- `top_n` integer not null
- `created_at` timestamptz not null
- `updated_at` timestamptz not null

### 3.3 `documents`

Purpose:

- Store uploaded document metadata

Fields:

- `id` UUID primary key
- `user_id` UUID not null
- `file_name` varchar(255) not null
- `file_ext` varchar(16) not null
- `file_size_bytes` bigint not null
- `file_hash` varchar(128) not null
- `storage_path` varchar(512) not null
- `source_encoding` varchar(32) not null
- `document_status` varchar(32) not null
- `error_message` text
- `created_at` timestamptz not null
- `updated_at` timestamptz not null

### 3.4 `indexing_jobs`

Purpose:

- Store indexing job lifecycle data

Fields:

- `id` UUID primary key
- `document_id` UUID not null
- `job_status` varchar(32) not null
- `current_stage` varchar(64) not null
- `processed_chunks` integer not null
- `total_chunks` integer not null
- `started_at` timestamptz
- `finished_at` timestamptz
- `error_message` text
- `created_at` timestamptz not null
- `updated_at` timestamptz not null

### 3.5 `document_chunks`

Purpose:

- Store persistent chunk records and vectors

Fields:

- `id` UUID primary key
- `document_id` UUID not null
- `chunk_index` integer not null
- `section_label` varchar(255)
- `content_text` text not null
- `content_summary` text
- `start_offset` integer not null
- `end_offset` integer not null
- `entity_list` jsonb not null
- `embedding_model` varchar(128) not null
- `embedding_vector` vector(1024) not null
- `content_hash` varchar(128) not null
- `created_at` timestamptz not null
- `updated_at` timestamptz not null

Indexes:

- B-tree index on `document_id`
- Unique index on `document_id, chunk_index`
- Vector index on `embedding_vector`

### 3.6 `chat_sessions`

Purpose:

- Store chat session headers

Fields:

- `id` UUID primary key
- `user_id` UUID not null
- `title` varchar(255) not null
- `session_status` varchar(32) not null
- `created_at` timestamptz not null
- `updated_at` timestamptz not null

### 3.7 `chat_messages`

Purpose:

- Store ordered messages inside a session

Fields:

- `id` UUID primary key
- `session_id` UUID not null
- `message_role` varchar(32) not null
- `message_index` integer not null
- `content_text` text not null
- `trace_id` UUID
- `created_at` timestamptz not null

Indexes:

- Unique index on `session_id, message_index`

### 3.8 `rag_query_traces`

Purpose:

- Store one trace per query execution

Fields:

- `id` UUID primary key
- `session_id` UUID not null
- `user_id` UUID not null
- `user_query` text not null
- `rewritten_query` text
- `retrieval_top_k` integer not null
- `rerank_top_n` integer not null
- `prompt_version` varchar(64) not null
- `answer_status` varchar(32) not null
- `answer_text` text
- `latency_ms` integer
- `error_message` text
- `created_at` timestamptz not null
- `updated_at` timestamptz not null

### 3.9 `trace_evidence_items`

Purpose:

- Store retrieved and reranked evidence records for a trace

Fields:

- `id` UUID primary key
- `trace_id` UUID not null
- `document_id` UUID not null
- `chunk_id` UUID not null
- `retrieval_rank` integer not null
- `retrieval_score` numeric(12,6) not null
- `rerank_rank` integer
- `rerank_score` numeric(12,6)
- `is_selected` boolean not null
- `citation_no` integer
- `created_at` timestamptz not null

Indexes:

- Index on `trace_id`
- Index on `trace_id, citation_no`

### 3.10 `document_feedback`

Purpose:

- Store user feedback on answer quality

Fields:

- `id` UUID primary key
- `trace_id` UUID not null
- `user_id` UUID not null
- `feedback_type` varchar(32) not null
- `feedback_text` text
- `created_at` timestamptz not null

## 4. Foreign Key Rules

Foreign keys:

- `user_settings.user_id` references `users.id`
- `documents.user_id` references `users.id`
- `indexing_jobs.document_id` references `documents.id`
- `document_chunks.document_id` references `documents.id`
- `chat_sessions.user_id` references `users.id`
- `chat_messages.session_id` references `chat_sessions.id`
- `rag_query_traces.session_id` references `chat_sessions.id`
- `rag_query_traces.user_id` references `users.id`
- `trace_evidence_items.trace_id` references `rag_query_traces.id`
- `trace_evidence_items.document_id` references `documents.id`
- `trace_evidence_items.chunk_id` references `document_chunks.id`
- `document_feedback.trace_id` references `rag_query_traces.id`
- `document_feedback.user_id` references `users.id`

## 5. Required Database Extensions

The following extension must be enabled:

```sql
create extension if not exists vector;
```

## 6. Required Baseline DDL Notes

All tables must include:

- Primary key
- Creation timestamp
- Update timestamp when the table is mutable

All mutable business records must support:

- Status field
- Error message field where failure state exists

## 7. Data Retention Rules

- Source documents are retained on local disk until manually deleted
- Metadata records are retained in PostgreSQL
- Trace records are retained for audit and analysis
- Chat messages are retained for session continuity
