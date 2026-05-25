-- ════════════════════════════════════════════════════════════════════════════
-- ExploratorX — Synthetic payment seed data
-- Purely synthetic, tokenized payment records. NEVER real PAN/CVV/PIN data.
-- Only safe fields: card_token, masked_pan, last4.
-- Used by the historical backfill (incremental snapshot) demo.
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO payment_transaction (
    transaction_id, card_token, masked_pan, last4, customer_id, merchant_id, merchant_name,
    terminal_id, channel, amount, currency, city, country, latitude, longitude,
    event_time, payment_status, auth_result, three_ds_status, device_id, ip_country, idempotency_key
) VALUES
    -- Normal approved sequence for one card
    ('TX-10001', 'TOK-AAA-001', '4111 11** **** 1111', '1111', 'CUST-001', 'MER-001', 'Berlin Coffee GmbH',
     'TERM-BER-01', 'POS', 12.50, 'EUR', 'Berlin', 'DE', 52.5200, 13.4050,
     NOW() - INTERVAL '90 minutes', 'APPROVED', 'OK', 'NOT_REQUIRED', 'DEV-001', 'DE', 'IDEM-10001'),
    ('TX-10002', 'TOK-AAA-001', '4111 11** **** 1111', '1111', 'CUST-001', 'MER-002', 'Berlin Bahn Shop',
     'TERM-BER-07', 'POS', 4.20, 'EUR', 'Berlin', 'DE', 52.5200, 13.4050,
     NOW() - INTERVAL '50 minutes', 'APPROVED', 'OK', 'NOT_REQUIRED', 'DEV-001', 'DE', 'IDEM-10002'),

    -- Impossible card travel: Berlin -> Munich within 3 minutes
    ('TX-20001', 'TOK-BBB-002', '5500 00** **** 0004', '0004', 'CUST-002', 'MER-010', 'Berlin Electronics',
     'TERM-BER-22', 'POS', 899.00, 'EUR', 'Berlin', 'DE', 52.5200, 13.4050,
     NOW() - INTERVAL '120 minutes', 'APPROVED', 'OK', 'PASSED', 'DEV-010', 'DE', 'IDEM-20001'),
    ('TX-20002', 'TOK-BBB-002', '5500 00** **** 0004', '0004', 'CUST-002', 'MER-011', 'Munich Luxury Store',
     'TERM-MUC-03', 'POS', 1499.00, 'EUR', 'Munich', 'DE', 48.1351, 11.5820,
     NOW() - INTERVAL '117 minutes', 'PENDING', NULL, 'CHALLENGE', 'DEV-011', 'DE', 'IDEM-20002'),

    -- Duplicate payment: same idempotency_key reused
    ('TX-30001', 'TOK-CCC-003', '3400 00** **** 0009', '0009', 'CUST-003', 'MER-020', 'Frankfurt Airport Duty',
     'TERM-FRA-09', 'ECOM', 250.00, 'EUR', 'Frankfurt', 'DE', 50.1109, 8.6821,
     NOW() - INTERVAL '80 minutes', 'APPROVED', 'OK', 'PASSED', 'DEV-020', 'DE', 'IDEM-DUP-1'),
    ('TX-30002', 'TOK-CCC-003', '3400 00** **** 0009', '0009', 'CUST-003', 'MER-020', 'Frankfurt Airport Duty',
     'TERM-FRA-09', 'ECOM', 250.00, 'EUR', 'Frankfurt', 'DE', 50.1109, 8.6821,
     NOW() - INTERVAL '79 minutes', 'PENDING', NULL, 'PASSED', 'DEV-020', 'DE', 'IDEM-DUP-1'),

    -- Velocity fraud: many rapid transactions on one card
    ('TX-40001', 'TOK-DDD-004', '4000 00** **** 0002', '0002', 'CUST-004', 'MER-030', 'Hamburg Online A',
     'TERM-HAM-01', 'ECOM', 39.90, 'EUR', 'Hamburg', 'DE', 53.5511, 9.9937,
     NOW() - INTERVAL '10 minutes', 'APPROVED', 'OK', 'NOT_REQUIRED', 'DEV-040', 'NL', 'IDEM-40001'),
    ('TX-40002', 'TOK-DDD-004', '4000 00** **** 0002', '0002', 'CUST-004', 'MER-031', 'Hamburg Online B',
     'TERM-HAM-02', 'ECOM', 59.90, 'EUR', 'Hamburg', 'DE', 53.5511, 9.9937,
     NOW() - INTERVAL '9 minutes', 'APPROVED', 'OK', 'NOT_REQUIRED', 'DEV-040', 'NL', 'IDEM-40002'),
    ('TX-40003', 'TOK-DDD-004', '4000 00** **** 0002', '0002', 'CUST-004', 'MER-032', 'Hamburg Online C',
     'TERM-HAM-03', 'ECOM', 79.90, 'EUR', 'Hamburg', 'DE', 53.5511, 9.9937,
     NOW() - INTERVAL '8 minutes', 'PENDING', 'DECLINED', 'NOT_REQUIRED', 'DEV-040', 'NL', 'IDEM-40003'),
    ('TX-40004', 'TOK-DDD-004', '4000 00** **** 0002', '0002', 'CUST-004', 'MER-033', 'Hamburg Online D',
     'TERM-HAM-04', 'ECOM', 99.90, 'EUR', 'Hamburg', 'DE', 53.5511, 9.9937,
     NOW() - INTERVAL '7 minutes', 'PENDING', 'DECLINED', 'NOT_REQUIRED', 'DEV-040', 'NL', 'IDEM-40004'),
    ('TX-40005', 'TOK-DDD-004', '4000 00** **** 0002', '0002', 'CUST-004', 'MER-034', 'Hamburg Online E',
     'TERM-HAM-05', 'ECOM', 119.90, 'EUR', 'Hamburg', 'DE', 53.5511, 9.9937,
     NOW() - INTERVAL '6 minutes', 'PENDING', 'DECLINED', 'NOT_REQUIRED', 'DEV-040', 'NL', 'IDEM-40005'),

    -- Geo mismatch: POS in DE but IP country differs
    ('TX-50001', 'TOK-EEE-005', '6011 00** **** 0004', '0004', 'CUST-005', 'MER-040', 'Hannover Web Shop',
     'TERM-HAN-01', 'ECOM', 320.00, 'EUR', 'Hannover', 'DE', 52.3759, 9.7320,
     NOW() - INTERVAL '30 minutes', 'PENDING', NULL, 'CHALLENGE', 'DEV-050', 'RU', 'IDEM-50001');
