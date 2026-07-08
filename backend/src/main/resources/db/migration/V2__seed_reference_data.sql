INSERT INTO currencies (
    code,
    name,
    decimal_places,
    created_at,
    updated_at
) VALUES
    ('BRL', 'Brazilian Real', 2, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('USD', 'US Dollar', 2, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

INSERT INTO receivable_types (
    code,
    description,
    monthly_spread,
    created_at,
    updated_at
) VALUES
    ('MERCANTILE_DUPLICATE', 'Mercantile Duplicate', 0.01500000, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
    ('POST_DATED_CHECK', 'Post-Dated Check', 0.02500000, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
