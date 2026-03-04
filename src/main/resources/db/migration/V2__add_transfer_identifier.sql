-- Apple 앱 이전 시 사용자 마이그레이션을 위한 transfer_identifier 컬럼 추가

ALTER TABLE "user"
    ADD COLUMN transfer_identifier VARCHAR(255);

-- 빠른 조회를 위한 인덱스 추가
CREATE INDEX idx_transfer_identifier
    ON "user"(transfer_identifier);

-- 컬럼 설명 추가
COMMENT ON COLUMN "user".transfer_identifier IS 'Apple 앱 이전 시 사용자 마이그레이션을 위한 transfer identifier';
