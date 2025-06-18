-- Migration script to re-enable the SourceToTargetTranslation question type

-- First ensure the question_types table exists
CREATE TABLE IF NOT EXISTS question_types (
    name TEXT PRIMARY KEY
);

-- Insert SourceToTargetTranslation question type if it doesn't exist
INSERT INTO question_types (name)
VALUES ('SourceToTargetTranslation')
ON CONFLICT (name) DO NOTHING;
