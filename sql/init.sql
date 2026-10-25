-- ИС «Фильмы»: схема БД и функции для специальных операций.
--
-- Запуск на helios:   psql -h pg -d studs -f sql/init.sql
-- Локально:           psql -h localhost -U postgres -d <база> -f sql/init.sql
--
-- ВНИМАНИЕ: скрипт пересоздаёт таблицы, существующие данные удаляются.

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

BEGIN;

DROP FUNCTION IF EXISTS get_movie_with_min_usa_box_office();
DROP FUNCTION IF EXISTS count_movies_with_usa_box_office_greater(real);
DROP FUNCTION IF EXISTS find_movies_by_tagline_substring(varchar);
DROP FUNCTION IF EXISTS find_screenwriters_without_oscars();
DROP FUNCTION IF EXISTS revoke_oscars_of_directors_by_genre(varchar);   -- прежняя версия операции 5
DROP FUNCTION IF EXISTS find_oscars_of_directors_by_genre(varchar);

DROP TABLE IF EXISTS movie CASCADE;
DROP TABLE IF EXISTS person CASCADE;
DROP TABLE IF EXISTS location CASCADE;
DROP TABLE IF EXISTS coordinates CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;

-- ---------------------------------------------------------------------------
-- Таблицы. id генерирует сама СУБД (identity-столбцы).
-- Внешние ключи ON DELETE RESTRICT: объект, на который ссылаются, удалить нельзя.
-- ---------------------------------------------------------------------------

CREATE TABLE coordinates (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    x  double precision NOT NULL,
    y  integer          NOT NULL
);

CREATE TABLE location (
    id   bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    x    integer      NOT NULL,
    y    integer      NOT NULL,
    name varchar(255)                                       -- может быть NULL
);

CREATE TABLE person (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        varchar(255) NOT NULL,
    eye_color   varchar(8),                                 -- может быть NULL
    hair_color  varchar(8)   NOT NULL,
    location_id bigint REFERENCES location (id) ON DELETE RESTRICT,
    birthday    date,                                       -- может быть NULL
    height      integer      NOT NULL,
    weight      integer      NOT NULL,
    passport_id varchar(46)  NOT NULL,

    CONSTRAINT person_name_not_blank        CHECK (name ~ '\S'),
    CONSTRAINT person_eye_color_valid       CHECK (eye_color IN ('RED', 'BLACK', 'BLUE', 'BROWN')),
    CONSTRAINT person_hair_color_valid      CHECK (hair_color IN ('RED', 'BLACK', 'BLUE', 'BROWN')),
    CONSTRAINT person_height_positive       CHECK (height > 0),
    CONSTRAINT person_weight_positive       CHECK (weight > 0),
    CONSTRAINT person_passport_id_not_blank CHECK (passport_id ~ '\S')
);

CREATE TABLE movie (
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name              varchar(255) NOT NULL,
    coordinates_id    bigint       NOT NULL REFERENCES coordinates (id) ON DELETE RESTRICT,
    creation_date     date         NOT NULL DEFAULT current_date,
    oscars_count      integer      NOT NULL,
    budget            bigint       NOT NULL,
    total_box_office  bigint       NOT NULL,
    mpaa_rating       varchar(8)   NOT NULL,
    director_id       bigint REFERENCES person (id) ON DELETE RESTRICT,
    screenwriter_id   bigint REFERENCES person (id) ON DELETE RESTRICT,
    operator_id       bigint       NOT NULL REFERENCES person (id) ON DELETE RESTRICT,
    length            integer      NOT NULL,
    golden_palm_count integer      NOT NULL,
    usa_box_office    real         NOT NULL,
    tagline           varchar(205),                         -- может быть NULL
    genre             varchar(16),                          -- может быть NULL

    CONSTRAINT movie_id_positive                CHECK (id > 0),
    CONSTRAINT movie_name_not_blank             CHECK (name ~ '\S'),
    -- oscarsCount может быть равен 0 (фильм без «Оскаров»), отрицательным — нельзя.
    CONSTRAINT movie_oscars_count_not_negative  CHECK (oscars_count >= 0),
    CONSTRAINT movie_budget_positive            CHECK (budget > 0),
    CONSTRAINT movie_total_box_office_positive  CHECK (total_box_office > 0),
    CONSTRAINT movie_mpaa_rating_valid          CHECK (mpaa_rating IN ('G', 'PG', 'R', 'NC_17')),
    CONSTRAINT movie_length_positive            CHECK (length > 0),
    CONSTRAINT movie_golden_palm_count_positive CHECK (golden_palm_count > 0),
    CONSTRAINT movie_usa_box_office_positive    CHECK (usa_box_office > 0 AND usa_box_office < 'Infinity'),
    CONSTRAINT movie_genre_valid                CHECK (genre IN ('DRAMA', 'COMEDY', 'ADVENTURE', 'THRILLER', 'FANTASY'))
);

-- Пользователи системы (регистрация и вход). Пароль хранится только в виде хеша.
-- Имена уникальны без учёта регистра.
CREATE TABLE app_user (
    id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      varchar(50)  NOT NULL,
    password_hash varchar(255) NOT NULL,

    CONSTRAINT app_user_username_valid CHECK (username ~ '^[A-Za-z0-9_.-]{3,50}$')
);

CREATE UNIQUE INDEX app_user_username_lower_idx ON app_user (lower(username));

CREATE INDEX person_location_idx    ON person (location_id);
CREATE INDEX movie_coordinates_idx  ON movie (coordinates_id);
CREATE INDEX movie_director_idx     ON movie (director_id);
CREATE INDEX movie_screenwriter_idx ON movie (screenwriter_id);
CREATE INDEX movie_operator_idx     ON movie (operator_id);

-- ---------------------------------------------------------------------------
-- Специальные операции. Вызываются из уровня бизнес-логики
-- (SpecialOperationsService -> SpecialOperationsRepository).
-- ---------------------------------------------------------------------------

-- 1. Один (любой) фильм с минимальным usaBoxOffice; при равенстве — с меньшим id.
CREATE FUNCTION get_movie_with_min_usa_box_office()
RETURNS SETOF movie
LANGUAGE sql STABLE AS $$
    SELECT * FROM movie ORDER BY usa_box_office, id LIMIT 1;
$$;

-- 2. Количество фильмов, у которых usaBoxOffice больше заданного.
CREATE FUNCTION count_movies_with_usa_box_office_greater(p_value real)
RETURNS bigint
LANGUAGE sql STABLE AS $$
    SELECT count(*) FROM movie WHERE usa_box_office > p_value;
$$;

-- 3. Фильмы, tagline которых содержит заданную подстроку (с учётом регистра, без шаблонов LIKE).
CREATE FUNCTION find_movies_by_tagline_substring(p_substring varchar)
RETURNS SETOF movie
LANGUAGE sql STABLE AS $$
    SELECT * FROM movie
    WHERE tagline IS NOT NULL AND strpos(tagline, p_substring) > 0
    ORDER BY id;
$$;

-- 4. Сценаристы, ни один фильм которых не получил ни одного «Оскара»:
--    человек указан сценаристом хотя бы одного фильма, и у всех таких фильмов oscars_count = 0.
CREATE FUNCTION find_screenwriters_without_oscars()
RETURNS SETOF person
LANGUAGE sql STABLE AS $$
    SELECT p.* FROM person p
    WHERE EXISTS (SELECT 1 FROM movie m WHERE m.screenwriter_id = p.id)
      AND NOT EXISTS (SELECT 1 FROM movie m WHERE m.screenwriter_id = p.id AND m.oscars_count > 0)
    ORDER BY p.id;
$$;

-- 5. Найти все «Оскары» режиссёров, снявших хотя бы один фильм в указанном жанре:
--    возвращает все фильмы таких режиссёров (суммарное число «Оскаров» считает бизнес-логика).
CREATE FUNCTION find_oscars_of_directors_by_genre(p_genre varchar)
RETURNS SETOF movie
LANGUAGE plpgsql STABLE AS $$
BEGIN
    IF p_genre IS NULL OR p_genre NOT IN ('DRAMA', 'COMEDY', 'ADVENTURE', 'THRILLER', 'FANTASY') THEN
        RAISE EXCEPTION 'Неизвестный жанр: %', p_genre;
    END IF;

    RETURN QUERY
    SELECT m.* FROM movie m
    WHERE m.director_id IN (SELECT g.director_id FROM movie g
                            WHERE g.genre = p_genre AND g.director_id IS NOT NULL)
    ORDER BY m.id;
END;
$$;

COMMIT;