-- Migration script to add the SourceToTargetTranslation question type to the database
CREATE TABLE IF NOT EXISTS question_types (
    name TEXT PRIMARY KEY
);

-- Insert supported question types
INSERT INTO question_types (name) VALUES
    ('FillInTheBlanks'),
    ('SourceToTargetTranslation');

-- Drop existing question type check constraints
ALTER TABLE questions DROP CONSTRAINT IF EXISTS questions_question_type_check;

-- Add foreign key constraints
ALTER TABLE questions ADD CONSTRAINT questions_question_type_fkey FOREIGN KEY (question_type) REFERENCES question_types (name);
