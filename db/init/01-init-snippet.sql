CREATE TABLE IF NOT EXISTS snippet (
    id_snippet SERIAL PRIMARY KEY,
    id_bucket INT,
    formatter_applied BOOLEAN,
    linter_applied BOOLEAN
    );

