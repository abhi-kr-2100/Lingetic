CREATE TABLE IF NOT EXISTS sentence_reviews (
    id UUID PRIMARY KEY,
    sentence_id UUID NOT NULL REFERENCES sentences(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL,
    language TEXT NOT NULL CHECK (language in ('English', 'Turkish')),
    repetitions SMALLINT NOT NULL DEFAULT 0,
    ease_factor REAL NOT NULL DEFAULT 2.5 CHECK (ease_factor >= 1.29),
    interval SMALLINT NOT NULL DEFAULT 0 CHECK (interval >= 0),
    next_review_instant TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (sentence_id, user_id)
);