CREATE TABLE IF NOT EXISTS "user" (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(36) UNIQUE NOT NULL,
    provider VARCHAR(10) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    deleted BOOLEAN DEFAULT false,
    UNIQUE (provider, provider_id)
);

CREATE INDEX IF NOT EXISTS idx_user_id ON "user"(user_id);
CREATE INDEX IF NOT EXISTS idx_provider ON "user"(provider, provider_id);
CREATE INDEX IF NOT EXISTS idx_deleted ON "user"(deleted);

CREATE TABLE IF NOT EXISTS currency (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) UNIQUE NOT NULL,
    symbol VARCHAR(10),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_currency_code ON currency(code);

CREATE TABLE IF NOT EXISTS country (
    id BIGSERIAL PRIMARY KEY,
    name_en VARCHAR(255) NOT NULL,
    name_kr VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL,
    capital VARCHAR(255),
    region VARCHAR(50) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    currency_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_code ON country(code);
CREATE INDEX IF NOT EXISTS idx_region ON country(region);
CREATE INDEX IF NOT EXISTS idx_currency_id ON country(currency_id);

CREATE TABLE IF NOT EXISTS exchange_rate (
    id BIGSERIAL PRIMARY KEY,
    currency_id BIGINT NOT NULL,
    rate DECIMAL(20,6) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_exchange_currency ON exchange_rate(currency_id);
CREATE INDEX IF NOT EXISTS idx_exchange_updated ON exchange_rate(updated_at);
