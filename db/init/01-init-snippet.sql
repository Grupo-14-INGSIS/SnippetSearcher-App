CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS snippet (
    id_snippet VARCHAR PRIMARY KEY,
    id_bucket VARCHAR NOT NULL,
    formatter_applied BOOLEAN DEFAULT TRUE,
    linter_applied BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS test
(
    id_test UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_snippet VARCHAR NOT NULL,
    in_put TEXT[],
    out_put TEXT
);