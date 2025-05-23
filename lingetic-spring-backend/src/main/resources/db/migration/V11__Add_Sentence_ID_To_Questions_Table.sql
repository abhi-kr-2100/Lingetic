-- Add sentence_id column to questions table with foreign key constraint
ALTER TABLE questions
ADD COLUMN IF NOT EXISTS sentence_id UUID REFERENCES sentences(id);