-- Add source_word_explanations column to questions table as JSONB
ALTER TABLE questions
ADD COLUMN IF NOT EXISTS source_word_explanations JSONB NOT NULL;
