-- V4__Change_Difficulty_To_Int.sql
-- Migration to change difficulty column from SMALLINT to INTEGER

ALTER TABLE questions
    ALTER COLUMN difficulty TYPE INTEGER USING difficulty::INTEGER;
