-- Add difficulty column to sentences table
ALTER TABLE sentences
ADD COLUMN IF NOT EXISTS difficulty INTEGER NOT NULL;

-- Remove difficulty column from questions table
ALTER TABLE questions
DROP COLUMN IF EXISTS difficulty;