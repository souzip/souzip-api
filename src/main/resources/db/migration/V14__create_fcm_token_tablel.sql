CREATE TABLE fcm_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    token VARCHAR(255) NOT NULL,
    device_type VARCHAR(20) NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    device_model VARCHAR(100),
    os_version VARCHAR(50),
    app_version VARCHAR(50),
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_used_at TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_fcm_tokens_user_id ON fcm_tokens(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_fcm_tokens_device_id ON fcm_tokens(device_id);
CREATE INDEX idx_fcm_tokens_device_type ON fcm_tokens(device_type);
CREATE INDEX idx_fcm_tokens_is_active ON fcm_tokens(is_active);
CREATE UNIQUE INDEX idx_fcm_tokens_token ON fcm_tokens(token);
CREATE UNIQUE INDEX idx_fcm_tokens_user_device ON fcm_tokens(user_id, device_id)
    WHERE user_id IS NOT NULL;
