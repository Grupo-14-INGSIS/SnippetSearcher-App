CREATE TABLE IF NOT EXISTS snippet (
    id_snippet SERIAL PRIMARY KEY,
    id_bucket INT,
    formatter_applied BOOLEAN,
    linter_applied BOOLEAN,
    );

CREATE TABLE IF NOT EXISTS test
(
    id_test SERIAL PRIMARY KEY,
    id_snippet INT,
    in_put JSONB,
    out_put TEXT,
);