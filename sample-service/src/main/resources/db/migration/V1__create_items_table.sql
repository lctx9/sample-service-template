-- Flyway naming: V<number>__<what_it_does>.sql
-- Never edit a script after it is merged. Add a new script instead.

CREATE TABLE items (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL
);
