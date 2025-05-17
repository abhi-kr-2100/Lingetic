package db

import (
	"context"
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"time"
)

type Question struct {
	ID                       string          // UUID
	QuestionType             string          // TEXT
	Language                 string          // TEXT
	Difficulty               int16           // SMALLINT
	QuestionListID           string          // UUID
	QuestionTypeSpecificData json.RawMessage // JSONB
}

type QuestionRepository struct {
	DB *sql.DB
}

func (r *QuestionRepository) GetQuestionByID(ctx context.Context, id string) (*Question, error) {
	const query = `
		SELECT id, question_type, language, difficulty, question_list_id, question_type_specific_data
		FROM questions WHERE id = $1
	`
	var question Question
	var questionTypeSpecificData []byte
	err := r.DB.QueryRowContext(ctx, query, id).Scan(
		&question.ID,
		&question.QuestionType,
		&question.Language,
		&question.Difficulty,
		&question.QuestionListID,
		&questionTypeSpecificData,
	)
	if err != nil {
		return nil, err
	}
	question.QuestionTypeSpecificData = questionTypeSpecificData
	return &question, nil
}

type QuestionReview struct {
	ID                string // UUID
	QuestionID        string // UUID
	UserID            string
	Language          string
	Repetitions       int
	EaseFactor        float32
	Interval          int
	NextReviewInstant time.Time
}

type ReviewRepository struct {
	DB           *sql.DB
	QuestionRepo *QuestionRepository
}

func (r *ReviewRepository) GetReviewForQuestionOrCreateNew(
	ctx context.Context, userID, questionID string,
) (*QuestionReview, error) {
	review := QuestionReview{}

	question, err := r.QuestionRepo.GetQuestionByID(ctx, questionID)
	if err != nil {
		return nil, fmt.Errorf("could not fetch question: %w", err)
	}

	const query = `
	WITH inserted AS (
		INSERT INTO question_reviews (
			id, question_id, user_id, language, repetitions, ease_factor, interval, next_review_instant
		) VALUES (
			gen_random_uuid(), $1, $2, $3, DEFAULT, DEFAULT, DEFAULT, DEFAULT
		)
		ON CONFLICT (question_id, user_id) DO NOTHING
		RETURNING id, question_id, user_id, language, repetitions, ease_factor, interval, next_review_instant
	)
	SELECT id, question_id, user_id, language, repetitions, ease_factor, interval, next_review_instant
	FROM inserted
	UNION ALL
	SELECT id, question_id, user_id, language, repetitions, ease_factor, interval, next_review_instant
	FROM question_reviews
	WHERE question_id = $1 AND user_id = $2 AND NOT EXISTS (SELECT 1 FROM inserted)
	LIMIT 1;
	`

	row := r.DB.QueryRowContext(ctx, query, questionID, userID, question.Language)
	err = row.Scan(
		&review.ID,
		&review.QuestionID,
		&review.UserID,
		&review.Language,
		&review.Repetitions,
		&review.EaseFactor,
		&review.Interval,
		&review.NextReviewInstant,
	)
	if err != nil {
		return nil, err
	}

	return &review, nil
}

func (r *ReviewRepository) Update(
	ctx context.Context, reviewID string, repetitions int, easeFactor float32, interval int, nextReviewInstant time.Time,
) error {
	_, err := r.DB.ExecContext(ctx,
		`UPDATE question_reviews SET
			repetitions = $1,
			ease_factor = $2,
			interval = $3,
			next_review_instant = $4
		WHERE id = $5`,
		repetitions, easeFactor, interval, nextReviewInstant, reviewID)
	return err
}

func MustEnv(name string) string {
	val := os.Getenv(name)
	if val == "" {
		log.Fatalf("Missing required environment variable: %s", name)
	}
	return val
}

func BuildDBConnURL() string {
	return fmt.Sprintf(
		"postgres://%s:%s@%s:%s/%s?sslmode=%s",
		MustEnv("DATABASE_USERNAME"),
		MustEnv("DATABASE_PASSWORD"),
		MustEnv("DATABASE_HOST"),
		MustEnv("DATABASE_PORT"),
		MustEnv("DATABASE_NAME"),
		MustEnv("DATABASE_SSLMODE"),
	)
}
