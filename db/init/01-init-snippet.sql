-- Igual que SnippetSearcher-Infrastructure/db/app-init/01-init-snippet.sql.
-- Coincide con las entidades Snippet, Test y UserData.
CREATE TABLE IF NOT EXISTS snippet
(
    id_snippet        VARCHAR(255) PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    bucket_id         VARCHAR(255) NOT NULL,
    formatter_applied BOOLEAN DEFAULT TRUE,
    language          VARCHAR(255) NOT NULL,
    -- Resultado del último linteo: TRUE cumple / FALSE no cumple / NULL todavía no se linteó
    compliant         BOOLEAN,
    description       TEXT DEFAULT '',
    version           TEXT DEFAULT '1.1'
);

CREATE TABLE IF NOT EXISTS test
(
    id_test    VARCHAR(255) PRIMARY KEY,
    id_snippet VARCHAR(255) NOT NULL,
    in_put     TEXT[],
    out_put    TEXT[],
    version    TEXT
);

-- Variables de entorno de cada test: una fila por variable
CREATE TABLE IF NOT EXISTS test_environment
(
    id_test   VARCHAR(255) NOT NULL REFERENCES test (id_test) ON DELETE CASCADE,
    env_name  VARCHAR(255) NOT NULL,
    env_value TEXT,
    PRIMARY KEY (id_test, env_name)
);

CREATE TABLE IF NOT EXISTS userdata
(
    id_user   VARCHAR(255) PRIMARY KEY,
    name_user VARCHAR(255)
);
