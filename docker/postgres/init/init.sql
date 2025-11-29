-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) UNIQUE NOT NULL,
    provider VARCHAR(10) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    UNIQUE (provider, provider_id)
);

CREATE INDEX IF NOT EXISTS idx_uuid ON users(uuid);
CREATE INDEX IF NOT EXISTS idx_provider ON users(provider, provider_id);
CREATE INDEX IF NOT EXISTS idx_deleted_at ON users(deleted_at);

-- 국가 테이블
CREATE TABLE IF NOT EXISTS countries (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    code VARCHAR(10) NOT NULL,
    capital VARCHAR(255),
    region VARCHAR(10) NOT NULL,
    flags VARCHAR(500),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_code ON countries(code);
CREATE INDEX IF NOT EXISTS idx_region ON countries(region);

CREATE TABLE IF NOT EXISTS exchange_rates (
    id BIGSERIAL PRIMARY KEY,
    base_code VARCHAR(10) NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    rate DECIMAL(20,6) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (base_code, currency_code)
);

CREATE INDEX IF NOT EXISTS idx_base_currency ON exchange_rates(base_code, currency_code);
CREATE INDEX IF NOT EXISTS idx_updated_at ON exchange_rates(updated_at);
