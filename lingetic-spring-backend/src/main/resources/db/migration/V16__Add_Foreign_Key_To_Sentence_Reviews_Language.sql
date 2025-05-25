-- First, remove the existing CHECK constraint on language column
ALTER TABLE sentence_reviews
DROP CONSTRAINT IF EXISTS sentence_reviews_language_check;

-- Then add foreign key constraint from sentence_reviews.language to languages.name
ALTER TABLE sentence_reviews
ADD CONSTRAINT fk_sentence_reviews_language
FOREIGN KEY (language) REFERENCES languages(name);
