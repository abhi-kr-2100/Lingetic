-- Remove question_list_id column from questions table
ALTER TABLE questions
DROP CONSTRAINT IF EXISTS fk_question_list;

ALTER TABLE questions
DROP COLUMN IF EXISTS question_list_id;

-- Drop question_lists table
DROP TABLE IF EXISTS question_lists;
