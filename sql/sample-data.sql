-- Демонстрационные данные (необязательно). Запускать после init.sql:
--   psql -h pg -d studs -f sql/sample-data.sql

SET client_encoding = 'UTF8';

BEGIN;

INSERT INTO coordinates (x, y) VALUES
    (10.5, 20),
    (-150.25, 7),
    (300, -12),
    (42.42, 42),
    (0, 0),
    (77.7, 100);

INSERT INTO location (x, y, name) VALUES
    (59, 30, 'Санкт-Петербург'),
    (55, 37, 'Москва'),
    (48, 2, 'Париж'),
    (35, 139, NULL);

INSERT INTO person (name, eye_color, hair_color, location_id, birthday, height, weight, passport_id)
SELECT v.name, v.eye, v.hair, l.id, v.birthday::date, v.height, v.weight, v.passport
FROM (VALUES
    ('Алексей Балабанов',  'BROWN', 'BLACK', 'Санкт-Петербург', '1959-02-25', 178, 74, '4001 123456'),
    ('Сергей Бодров',      'BLUE',  'BROWN', 'Москва',          '1971-12-27', 186, 78, '4501 654321'),
    ('Андрей Тарковский',  'BROWN', 'BLACK', 'Москва',          '1932-04-04', 172, 65, '4502 111222'),
    ('Аркадий Стругацкий', NULL,    'BROWN', NULL,              '1925-08-28', 175, 80, '4503 333444'),
    ('Вадим Юсов',         'BLUE',  'RED',   'Москва',          NULL,         180, 82, '4504 555666'),
    ('Люк Бессон',         'BLUE',  'BROWN', 'Париж',           '1959-03-18', 175, 90, 'FR 12AB34567'),
    ('Тьерри Арбогаст',    NULL,    'BLACK', 'Париж',           NULL,         181, 77, 'FR 98ZY76543'),
    ('Хаяо Миядзаки',      'BLACK', 'BLACK', NULL,              '1941-01-05', 165, 60, 'JP TK0001234')
) AS v(name, eye, hair, city, birthday, height, weight, passport)
LEFT JOIN location l ON l.name = v.city;

INSERT INTO movie (name, coordinates_id, oscars_count, budget, total_box_office, mpaa_rating,
                   director_id, screenwriter_id, operator_id, length, golden_palm_count,
                   usa_box_office, tagline, genre)
SELECT v.name, c.id, v.oscars, v.budget, v.total, v.mpaa, d.id, s.id, o.id, v.length, v.palms,
       v.usa, v.tagline, v.genre
FROM (VALUES
    ('Брат',               1, 0, 10000000,  1500000,   'R',     'Алексей Балабанов', 'Алексей Балабанов',  'Сергей Бодров',     96, 1, 120000.5,  'Сила в правде',                    'DRAMA'),
    ('Брат 2',             2, 0, 1500000,   1700000,   'R',     'Алексей Балабанов', 'Алексей Балабанов',  'Сергей Бодров',     127, 1, 250000,   'Сила в правде, брат',              'THRILLER'),
    ('Сталкер',            3, 3, 1000000,   4300000,   'PG',    'Андрей Тарковский', 'Аркадий Стругацкий', 'Вадим Юсов',        161, 2, 98000,    'Зона исполняет желания',           'FANTASY'),
    ('Солярис',            4, 4, 1000000,   3000000,   'PG',    'Андрей Тарковский', 'Аркадий Стругацкий', 'Вадим Юсов',        167, 3, 87000.75, NULL,                               'FANTASY'),
    ('Леон',               5, 5, 16000000,  45284974,  'R',     'Люк Бессон',        'Люк Бессон',         'Тьерри Арбогаст',   110, 1, 19501238, 'Если хочешь сделать хорошо — сделай сам', 'THRILLER'),
    ('Пятый элемент',      6, 1, 90000000,  263920180, 'PG',    'Люк Бессон',        NULL,                 'Тьерри Арбогаст',   126, 1, 63820180, 'Свет, вода, земля, огонь — и пятый', 'ADVENTURE'),
    ('Унесённые призраками', 1, 1, 19000000, 395580000, 'PG',   'Хаяо Миядзаки',     'Хаяо Миядзаки',      'Хаяо Миядзаки',     125, 2, 10055859, NULL,                               'FANTASY'),
    ('Мой сосед Тоторо',   2, 2, 3700000,   41000000,  'G',     NULL,                'Хаяо Миядзаки',      'Тьерри Арбогаст',   86,  1, 1105564,  'Сосед, которого видят только дети', 'COMEDY')
) AS v(name, coord_no, oscars, budget, total, mpaa, director, screenwriter, operator, length, palms, usa, tagline, genre)
JOIN coordinates c ON c.id = (SELECT id FROM coordinates ORDER BY id OFFSET v.coord_no - 1 LIMIT 1)
LEFT JOIN person d ON d.name = v.director
LEFT JOIN person s ON s.name = v.screenwriter
JOIN person o ON o.name = v.operator;

COMMIT;