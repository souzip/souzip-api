CREATE TABLE IF NOT EXISTS notice (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_notice_status ON notice(status);
CREATE INDEX idx_notice_created_at ON notice(created_at DESC);
CREATE INDEX idx_notice_author_id ON notice(author_id);
