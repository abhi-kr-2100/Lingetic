CREATE TABLE IF NOT EXISTS question_lists (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    language TEXT NOT NULL CHECK (language in ('English', 'Turkish'))
);

-- Add a foreign key constraint to the questions table
ALTER TABLE questions
ADD CONSTRAINT fk_question_list
FOREIGN KEY (question_list_id) REFERENCES question_lists(id);
