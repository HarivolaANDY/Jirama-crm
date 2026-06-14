-- V12: Seed Madagascar administrative regions
-- Source: Madagascar administrative divisions (Faritany + Faritra)

-- Provinces (Faritany)
INSERT INTO regions (code, name, type) VALUES
    ('TANA', 'Antananarivo', 'province'),
    ('TAM', 'Toamasina', 'province'),
    ('MAJ', 'Mahajanga', 'province'),
    ('FIA', 'Fianarantsoa', 'province'),
    ('TOL', 'Toliara', 'province'),
    ('ANT', 'Antsiranana', 'province');

-- Regions (Faritra) under Antananarivo province
INSERT INTO regions (code, name, type, parent_id) VALUES
    ('ANA', 'Analamanga', 'region', (SELECT id FROM regions WHERE code = 'TANA')),
    ('BON', 'Bongolava', 'region', (SELECT id FROM regions WHERE code = 'TANA')),
    ('ITA', 'Itasy', 'region', (SELECT id FROM regions WHERE code = 'TANA')),
    ('VAK', 'Vakinankaratra', 'region', (SELECT id FROM regions WHERE code = 'TANA'));

-- Regions under Toamasina province
INSERT INTO regions (code, name, type, parent_id) VALUES
    ('ATS', 'Atsinanana', 'region', (SELECT id FROM regions WHERE code = 'TAM')),
    ('ALA', 'Alaotra-Mangoro', 'region', (SELECT id FROM regions WHERE code = 'TAM')),
    ('ANO', 'Analanjirofo', 'region', (SELECT id FROM regions WHERE code = 'TAM'));

-- Regions under Mahajanga province
INSERT INTO regions (code, name, type, parent_id) VALUES
    ('BOE', 'Boeny', 'region', (SELECT id FROM regions WHERE code = 'MAJ')),
    ('BET', 'Betsiboka', 'region', (SELECT id FROM regions WHERE code = 'MAJ')),
    ('MEL', 'Melaky', 'region', (SELECT id FROM regions WHERE code = 'MAJ')),
    ('SOF', 'Sofia', 'region', (SELECT id FROM regions WHERE code = 'MAJ'));

-- Regions under Fianarantsoa province
INSERT INTO regions (code, name, type, parent_id) VALUES
    ('FIA_', 'Fitovinany', 'region', (SELECT id FROM regions WHERE code = 'FIA')),
    ('HAU', 'Haute Matsiatra', 'region', (SELECT id FROM regions WHERE code = 'FIA')),
    ('IHO', 'Ihorombe', 'region', (SELECT id FROM regions WHERE code = 'FIA')),
    ('ATS_F', 'Atsimo-Atsinanana', 'region', (SELECT id FROM regions WHERE code = 'FIA')),
    ('AMO', 'Amoron''i Mania', 'region', (SELECT id FROM regions WHERE code = 'FIA')),
    ('VAT', 'Vatovavy', 'region', (SELECT id FROM regions WHERE code = 'FIA'));

-- Regions under Toliara province
INSERT INTO regions (code, name, type, parent_id) VALUES
    ('ATS_T', 'Atsimo-Andrefana', 'region', (SELECT id FROM regions WHERE code = 'TOL')),
    ('AND', 'Androy', 'region', (SELECT id FROM regions WHERE code = 'TOL')),
    ('ANU', 'Anosy', 'region', (SELECT id FROM regions WHERE code = 'TOL')),
    ('MEN', 'Menabe', 'region', (SELECT id FROM regions WHERE code = 'TOL'));

-- Regions under Antsiranana province
INSERT INTO regions (code, name, type, parent_id) VALUES
    ('DIA', 'Diana', 'region', (SELECT id FROM regions WHERE code = 'ANT')),
    ('SAV', 'Sava', 'region', (SELECT id FROM regions WHERE code = 'ANT'));

-- Major communes (district capitals)
INSERT INTO regions (code, name, type, parent_id) VALUES
    ('TNR', 'Antananarivo Renivohitra', 'commune', (SELECT id FROM regions WHERE code = 'ANA')),
    ('TSI', 'Antsirabe', 'commune', (SELECT id FROM regions WHERE code = 'VAK')),
    ('TMA', 'Toamasina', 'commune', (SELECT id FROM regions WHERE code = 'ATS')),
    ('FNR', 'Fianarantsoa', 'commune', (SELECT id FROM regions WHERE code = 'HAU')),
    ('MJG', 'Mahajanga', 'commune', (SELECT id FROM regions WHERE code = 'BOE')),
    ('TLR', 'Toliara', 'commune', (SELECT id FROM regions WHERE code = 'ATS_T')),
    ('DNS', 'Antsiranana', 'commune', (SELECT id FROM regions WHERE code = 'DIA'));
