-- =============================================================
-- Space Mission Control - Seed Data
-- V2__seed_data.sql
-- =============================================================

-- Seed missions
INSERT INTO missions (name, description, status, destination, launch_date) VALUES
('Artemis VII',    'Return humans to the lunar south pole and establish base camp', 'LAUNCH_READY',  'Moon - South Pole',     '2026-07-04T08:00:00'),
('Ares I',         'First crewed Mars surface mission. 18-month stay at Jezero Crater', 'TRAINING', 'Mars - Jezero Crater',  '2027-03-15T12:00:00'),
('Hermes Station', 'Construct Phase 2 of the Hermes deep-space waypoint station',        'CREW_SELECTION', 'L2 Lagrange Point', NULL),
('Europa Dive',    'Deploy autonomous submersible through Europa ice shell',              'PLANNING', 'Jupiter - Europa',      '2031-01-01T00:00:00');

-- Seed crew members
INSERT INTO crew_members (first_name, last_name, email, role, callsign, flight_hours) VALUES
('Elena',    'Vasquez',   'e.vasquez@nasa.gov',   'COMMANDER',          'Falcon',   4200),
('James',    'Park',      'j.park@nasa.gov',       'PILOT',              'Viper',    3100),
('Amara',    'Osei',      'a.osei@esa.int',         'MISSION_SPECIALIST', 'Phoenix',  1800),
('Dmitri',   'Sorokin',   'd.sorokin@roscosmos.ru', 'FLIGHT_ENGINEER',    'Bear',     2750),
('Yuki',     'Tanaka',    'y.tanaka@jaxa.jp',       'SCIENCE_OFFICER',    'Sakura',   950),
('Marcus',   'Webb',      'm.webb@nasa.gov',         'MEDICAL_OFFICER',    'Doc',      1200),
('Priya',    'Sharma',    'p.sharma@isro.in',        'PAYLOAD_SPECIALIST', 'Spark',    600),
('Ravi',     'Nair',      'r.nair@isro.in',          'FLIGHT_ENGINEER',    'Arrow',    1450),
('Sofia',    'Lindgren',  's.lindgren@esa.int',      'COMMANDER',          'Wolf',     5100),
('Carlos',   'Mendoza',   'c.mendoza@nasa.gov',      'PILOT',              'Eagle',    3800);

-- Assign crew to Artemis VII (id=1)
UPDATE crew_members SET mission_id = 1, assigned_at = GETDATE()
WHERE email IN ('e.vasquez@nasa.gov', 'j.park@nasa.gov', 'a.osei@esa.int', 'd.sorokin@roscosmos.ru', 'm.webb@nasa.gov');

-- Assign crew to Ares I (id=2)
UPDATE crew_members SET mission_id = 2, assigned_at = GETDATE()
WHERE email IN ('s.lindgren@esa.int', 'c.mendoza@nasa.gov', 'y.tanaka@jaxa.jp', 'p.sharma@isro.in');
