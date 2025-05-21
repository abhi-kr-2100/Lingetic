-- Add source_word_explanations column to sentences table as JSONB
ALTER TABLE sentences
ADD COLUMN IF NOT EXISTS source_word_explanations JSONB NOT NULL DEFAULT '[]'::jsonb;
