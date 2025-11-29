-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '회원 ID (내부용)',
    uuid VARCHAR(36) UNIQUE NOT NULL COMMENT '회원 UUID (외부 노출용)',
    provider ENUM('KAKAO', 'APPLE') UNIQUE NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    INDEX idx_uuid (uuid),
    INDEX idx_provider (provider, provider_id),
    INDEX idx_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS countries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    code VARCHAR(10) NOT NULL,
    capital VARCHAR(255),
    region ENUM('Asia', 'Africa', 'Americas', 'Europe', 'Oceania') NOT NULL,
    flags VARCHAR(500),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_region (region)
);

CREATE TABLE IF NOT EXISTS exchange_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    base_code VARCHAR(10) NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    rate DECIMAL(20, 6) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_base_currency (base_code, currency_code),
    INDEX idx_updated_at (updated_at),
    UNIQUE KEY unique_currency_pair (base_code, currency_code)
);
