-- V4: Add chat_session_documents join table for document library isolation
-- Each chat session can be scoped to specific documents

CREATE TABLE chat_session_documents (
    session_id VARCHAR(36) NOT NULL,
    document_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (session_id, document_id),
    CONSTRAINT fk_csd_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_csd_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

CREATE INDEX idx_csd_session_id ON chat_session_documents(session_id);
