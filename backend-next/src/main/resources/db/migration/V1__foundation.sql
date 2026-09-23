-- Independent replacement history. Never apply to the legacy wayline database.
CREATE TABLE foundation_metadata (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    application VARCHAR(64) NOT NULL CHECK (application = 'wayline-next'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO foundation_metadata (id, application) VALUES (1, 'wayline-next');
