-- nickname 컬럼을 nullable로 변경
ALTER TABLE "user"
ALTER COLUMN nickname DROP NOT NULL;
