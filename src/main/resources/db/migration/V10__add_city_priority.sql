ALTER TABLE city ADD COLUMN priority INT NULL;

CREATE INDEX idx_city_priority ON city (country_id, priority);
