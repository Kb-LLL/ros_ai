CREATE TABLE IF NOT EXISTS conversation_document_binding (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_conversation_document_binding (conversation_id, document_id),
    KEY idx_cdb_conversation_id (conversation_id),
    KEY idx_cdb_document_id (document_id)
);
