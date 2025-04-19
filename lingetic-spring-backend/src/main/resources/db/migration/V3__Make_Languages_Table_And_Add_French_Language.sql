-- Create languages table with name as primary key
CREATE TABLE IF NOT EXISTS languages (
    name TEXT PRIMARY KEY
);

-- Insert supported languages
INSERT INTO languages (name) VALUES
    ('English'),
    ('Turkish'),
    ('French');

-- Drop existing language check constraints
ALTER TABLE question_lists DROP CONSTRAINT IF EXISTS question_lists_language_check;
ALTER TABLE questions DROP CONSTRAINT IF EXISTS questions_language_check;
ALTER TABLE question_reviews DROP CONSTRAINT IF EXISTS question_reviews_language_check;

-- Add foreign key constraints
ALTER TABLE question_lists
    ADD CONSTRAINT fk_question_lists_language
    FOREIGN KEY (language) REFERENCES languages(name);

ALTER TABLE questions
    ADD CONSTRAINT fk_questions_language
    FOREIGN KEY (language) REFERENCES languages(name);

ALTER TABLE question_reviews
    ADD CONSTRAINT fk_question_reviews_language
    FOREIGN KEY (language) REFERENCES languages(name);
