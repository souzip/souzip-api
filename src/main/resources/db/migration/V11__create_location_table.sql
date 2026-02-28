CREATE TABLE location (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 이름 검색용 인덱스
CREATE INDEX idx_location_name ON location (name);

-- 근처 장소 찾기용 좌표 인덱스
CREATE INDEX idx_location_coordinate ON location (latitude, longitude);
