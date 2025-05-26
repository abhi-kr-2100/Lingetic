-- Create the sentences table
CREATE TABLE IF NOT EXISTS sentences (
    id UUID PRIMARY KEY,
    source_language TEXT NOT NULL REFERENCES languages(name),
    source_text TEXT NOT NULL,
    translation_language TEXT NOT NULL REFERENCES languages(name),
    translation_text TEXT NOT NULL
);
