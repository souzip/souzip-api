ALTER TABLE souvenir
    ADD COLUMN IF NOT EXISTS converted_amount INTEGER,
    ADD COLUMN IF NOT EXISTS converted_currency VARCHAR(3);

UPDATE souvenir s
SET
    original_amount = s.local_price,
    original_currency = c.code
    FROM currency c
WHERE s.currency_symbol = c.symbol
  AND s.local_price IS NOT NULL
  AND s.currency_symbol IS NOT NULL
  AND s.original_amount IS NULL;

UPDATE souvenir
SET
    converted_amount = exchange_amount,
    converted_currency = 'KRW'
WHERE original_currency IS NOT NULL
  AND original_amount IS NOT NULL
  AND original_currency != 'KRW'
  AND converted_amount IS NULL;

UPDATE souvenir s
SET
    converted_currency = c2.code,
    converted_amount = CASE
           WHEN r.rate IS NOT NULL THEN ROUND(s.original_amount / r.rate)
           ELSE NULL
        END
    FROM country c
JOIN currency c2 ON c.currency_id = c2.id
    LEFT JOIN exchange_rate r ON r.currency_code = c2.code AND r.base_code = 'KRW'
WHERE s.country_code = c.code
  AND s.original_currency = 'KRW'
  AND s.original_amount IS NOT NULL
  AND s.converted_currency IS NULL;
