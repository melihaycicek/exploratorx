-- ════════════════════════════════════════════════════════════════════════════
-- ExploratorX — Synthetic CDR seed data
-- Purely synthetic German telecom signals. No real subscriber or personal data.
-- Used by the historical backfill (incremental snapshot) demo.
-- ════════════════════════════════════════════════════════════════════════════

INSERT INTO cdr_signal (subscriber_id, event_time, city, latitude, longitude, cell_id, signal_type) VALUES
    -- Normal mobility: subscriber moving plausibly over time
    ('SUB-1001', NOW() - INTERVAL '90 minutes', 'Berlin',     52.5200, 13.4050, 'CELL-BER-001', 'VOICE'),
    ('SUB-1001', NOW() - INTERVAL '40 minutes', 'Leipzig',    51.3397, 12.3731, 'CELL-LEI-014', 'DATA'),
    ('SUB-1001', NOW() - INTERVAL '5 minutes',  'Leipzig',    51.3397, 12.3731, 'CELL-LEI-009', 'VOICE'),

    -- Plausible commute (Cologne -> Düsseldorf, short hop)
    ('SUB-1002', NOW() - INTERVAL '60 minutes', 'Cologne',    50.9333,  6.9500, 'CELL-COL-021', 'DATA'),
    ('SUB-1002', NOW() - INTERVAL '35 minutes', 'Dusseldorf', 51.2217,  6.7762, 'CELL-DUS-007', 'VOICE'),

    -- Impossible travel: Berlin -> Munich within 3 minutes (historical sample)
    ('SUB-2001', NOW() - INTERVAL '120 minutes', 'Berlin',    52.5200, 13.4050, 'CELL-BER-099', 'VOICE'),
    ('SUB-2001', NOW() - INTERVAL '117 minutes', 'Munich',    48.1351, 11.5820, 'CELL-MUC-099', 'VOICE'),

    -- Split signal: same timestamp, two different cities
    ('SUB-2002', NOW() - INTERVAL '200 minutes', 'Hamburg',   53.5511,  9.9937, 'CELL-HAM-050', 'DATA'),
    ('SUB-2002', NOW() - INTERVAL '200 minutes', 'Frankfurt', 50.1109,  8.6821, 'CELL-FRA-050', 'DATA'),

    -- Suspicious (fast but not impossible): Stuttgart -> Frankfurt in ~25 min
    ('SUB-3001', NOW() - INTERVAL '70 minutes', 'Stuttgart',  48.7758,  9.1829, 'CELL-STU-011', 'VOICE'),
    ('SUB-3001', NOW() - INTERVAL '45 minutes', 'Frankfurt',  50.1109,  8.6821, 'CELL-FRA-011', 'VOICE');
