-- 1. souvenir 테이블: is_owned 컬럼 제거
ALTER TABLE souvenir DROP COLUMN IF EXISTS is_owned;

-- 2. file 테이블: souvenir_id 컬럼 제거
ALTER TABLE file DROP COLUMN IF EXISTS souvenir_id;
