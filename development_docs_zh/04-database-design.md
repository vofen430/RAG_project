# 数据库设计文档

## 1. 数据库平台

- PostgreSQL 16
- 启用 pgvector 扩展

## 2. 数据库命名

- 数据库名：`rag_local`
- Schema 名：`public`

## 3. 核心表

### 3.1 `users`

用途：

- 存储本地应用用户账户

字段：

- `id` UUID primary key
- `username` varchar(64) unique not null
- `password_hash` varchar(255) not null
- `display_name` varchar(128) not null
- `role_code` varchar(32) not null
- `status` varchar(32) not null
- `created_at` timestamptz not null
- `updated_at` timestamptz not null

### 3.2 `user_settings`

用途：

- 存储每个用户的应用设置

字段：

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

用途：

- 存储上传文档的元数据

字段：

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

用途：

- 存储索引任务生命周期数据

字段：

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

用途：

- 存储持久化 chunk 记录和向量

字段：

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

索引：

- `document_id` 上建立 B-tree 索引
- `document_id, chunk_index` 上建立唯一索引
- `embedding_vector` 上建立向量索引

### 3.6 `chat_sessions`

用途：

- 存储聊天会话头信息

字段：

- `id` UUID primary key
- `user_id` UUID not null
- `title` varchar(255) not null
- `session_status` varchar(32) not null
- `created_at` timestamptz not null
- `updated_at` timestamptz not null

### 3.7 `chat_messages`

用途：

- 存储会话内有序消息

字段：

- `id` UUID primary key
- `session_id` UUID not null
- `message_role` varchar(32) not null
- `message_index` integer not null
- `content_text` text not null
- `trace_id` UUID
- `created_at` timestamptz not null

索引：

- `session_id, message_index` 上建立唯一索引

### 3.8 `rag_query_traces`

用途：

- 每次 query 执行保存一条 trace

字段：

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

用途：

- 保存一条 trace 的召回和 rerank 证据记录

字段：

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

索引：

- `trace_id` 上建立索引
- `trace_id, citation_no` 上建立索引

### 3.10 `document_feedback`

用途：

- 存储用户对答案质量的反馈

字段：

- `id` UUID primary key
- `trace_id` UUID not null
- `user_id` UUID not null
- `feedback_type` varchar(32) not null
- `feedback_text` text
- `created_at` timestamptz not null

## 4. 外键规则

外键如下：

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

## 5. 必需数据库扩展

必须启用以下扩展：

```sql
create extension if not exists vector;
```

## 6. 必需 DDL 基线说明

所有表都必须包含：

- 主键
- 创建时间戳
- 对可变表包含更新时间戳

所有可变业务记录都必须支持：

- 状态字段
- 存在失败状态时的错误信息字段

## 7. 数据保留规则

- 源文档保留在本地磁盘，直到人工删除
- 元数据记录保留在 PostgreSQL
- trace 记录保留用于审计与分析
- 聊天消息保留用于会话连续性
