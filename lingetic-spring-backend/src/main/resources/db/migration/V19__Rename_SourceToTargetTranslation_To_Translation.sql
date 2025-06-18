-- Migration script to rename SourceToTargetTranslation question type to Translation

-- First ensure the question_types table exists
CREATE TABLE IF NOT EXISTS question_types (
    name TEXT PRIMARY KEY
);

-- Update existing questions to use the new type name
-- Not needed since Translation questions are not stored in DB
-- UPDATE questions
-- SET type = 'Translation'
-- WHERE type = 'SourceToTargetTranslation';

-- Remove the old question type
DELETE FROM question_types
WHERE name = 'SourceToTargetTranslation';

-- Insert the new question type
INSERT INTO question_types (name)
VALUES ('Translation');
