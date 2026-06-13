CREATE TABLE push_broadcast_histories (
    id BIGSERIAL PRIMARY KEY,
    admin_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    total_targets INT NOT NULL,
    success_count INT NOT NULL,
    fail_count INT NOT NULL,
    firebase_configured BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_push_broadcast_histories_created_at ON push_broadcast_histories (created_at DESC);
