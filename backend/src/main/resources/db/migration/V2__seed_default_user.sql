-- V2: Seed default admin user and user settings
-- Password: demo-password (BCrypt hash)
INSERT INTO users (id, username, password_hash, display_name, role_code, status, created_at, updated_at)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'demo',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Demo User',
    'ADMIN',
    'ACTIVE',
    now(),
    now()
);

INSERT INTO user_settings (id, user_id, embedding_model, rerank_model, chat_model, chunk_size, chunk_overlap, top_k, top_n, created_at, updated_at)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000001',
    'Qwen/Qwen3-Embedding-0.6B',
    'Qwen/Qwen3-Reranker-0.6B',
    'Qwen/Qwen3-8B',
    500,
    100,
    10,
    5,
    now(),
    now()
);
