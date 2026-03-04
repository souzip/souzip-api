ALTER TABLE souvenir
    ADD COLUMN IF NOT EXISTS original_amount INTEGER,
    ADD COLUMN IF NOT EXISTS original_currency VARCHAR(3),
    ADD COLUMN IF NOT EXISTS exchange_amount INTEGER;

UPDATE souvenir s
SET
    original_amount = s.local_price,
    original_currency = c.code,
    exchange_amount = s.krw_price
    FROM currency c
WHERE s.currency_symbol = c.symbol
  AND s.local_price IS NOT NULL
  AND s.original_amount IS NULL;
