package db

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"os"
	"time"

	"github.com/google/uuid"
)

type Sentence struct {
	SourceLanguage string `json:"sourceLanguage"`
}

type SentenceReview struct {
	ID                string    `json:"id"`
	SentenceID        string    `json:"sentenceId"`
	UserID            string    `json:"userId"`
	Language          string    `json:"language"`
	Repetitions       int       `json:"repetitions"`
	EaseFactor        float64   `json:"easeFactor"`
	Interval          int       `json:"interval"`
	NextReviewInstant time.Time `json:"nextReviewInstant"`
}

type SentenceRepository struct {
	DB *sql.DB
}

func NewSentenceRepository(db *sql.DB) *SentenceRepository {
	return &SentenceRepository{DB: db}
}
func (repo *SentenceRepository) GetByID(ctx context.Context, id string) (*Sentence, error) {
	query := `
		SELECT source_language
		FROM sentences
		WHERE id = $1
	`

	var sentence Sentence

	err := repo.DB.QueryRowContext(ctx, query, id).Scan(
		&sentence.SourceLanguage,
	)

	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, fmt.Errorf("sentence not found: %w", err)
		}
		return nil, fmt.Errorf("error fetching sentence: %w", err)
	}

	return &sentence, nil
}

type ReviewRepository struct {
	DB           *sql.DB
	SentenceRepo *SentenceRepository
}

func NewReviewRepository(db *sql.DB, sentenceRepo *SentenceRepository) *ReviewRepository {
	return &ReviewRepository{
		DB:           db,
		SentenceRepo: sentenceRepo,
	}
}

func (repo *ReviewRepository) GetReviewForSentenceOrCreateNew(
	ctx context.Context,
	userID, sentenceID string,
) (*SentenceReview, error) {
	tx, err := repo.DB.BeginTx(ctx, nil)
	if err != nil {
		return nil, fmt.Errorf("error beginning transaction: %w", err)
	}
	defer tx.Rollback()

	// Try to get existing review
	review, err := repo.getReviewForSentence(ctx, tx, userID, sentenceID)
	if err == nil {
		return review, tx.Commit()
	}

	// If review doesn't exist, create a new one
	sentence, err := repo.SentenceRepo.GetByID(ctx, sentenceID)
	if err != nil {
		return nil, fmt.Errorf("error getting sentence: %w", err)
	}

	reviewID := uuid.New().String()
	review = &SentenceReview{
		ID:                reviewID,
		SentenceID:        sentenceID,
		UserID:            userID,
		Language:          sentence.SourceLanguage,
		Repetitions:       0,
		EaseFactor:        2.5,
		Interval:          0,
		NextReviewInstant: time.Now(),
	}

	query := `
		INSERT INTO sentence_reviews (
			id, sentence_id, user_id, language, repetitions,
			ease_factor, interval, next_review_instant
		) VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
		RETURNING id
	`

	err = tx.QueryRowContext(
		ctx,
		query,
		review.ID,
		review.SentenceID,
		review.UserID,
		review.Language,
		review.Repetitions,
		review.EaseFactor,
		review.Interval,
		review.NextReviewInstant,
	).Scan(&review.ID)

	if err != nil {
		return nil, fmt.Errorf("error creating review: %w", err)
	}

	return review, tx.Commit()
}

func (r *ReviewRepository) Update(ctx context.Context, updatedReview *SentenceReview) error {
	query := `
		UPDATE sentence_reviews
		SET
			repetitions = $1,
			ease_factor = $2,
			interval = $3,
			next_review_instant = $4
		WHERE id = $5
	`

	result, err := r.DB.ExecContext(
		ctx,
		query,
		updatedReview.Repetitions,
		updatedReview.EaseFactor,
		updatedReview.Interval,
		updatedReview.NextReviewInstant,
		updatedReview.ID,
	)

	if err != nil {
		return fmt.Errorf("error updating review: %w", err)
	}

	rowsAffected, err := result.RowsAffected()
	if err != nil {
		return fmt.Errorf("error getting rows affected: %w", err)
	}

	if rowsAffected == 0 {
		return fmt.Errorf("no review found with ID %s", updatedReview.ID)
	}

	return nil
}

func (r *ReviewRepository) getReviewForSentence(
	ctx context.Context,
	tx *sql.Tx,
	userID, sentenceID string,
) (*SentenceReview, error) {
	query := `
		SELECT
			id, sentence_id, user_id, language, repetitions,
			ease_factor, interval, next_review_instant
		FROM sentence_reviews
		WHERE user_id = $1 AND sentence_id = $2
	`

	var review SentenceReview

	err := tx.QueryRowContext(ctx, query, userID, sentenceID).Scan(
		&review.ID,
		&review.SentenceID,
		&review.UserID,
		&review.Language,
		&review.Repetitions,
		&review.EaseFactor,
		&review.Interval,
		&review.NextReviewInstant,
	)

	if err != nil {
		return nil, fmt.Errorf("error fetching review: %w", err)
	}

	return &review, nil
}

func BuildDBConnURL() string {
	host := getEnv("DATABASE_HOST")
	port := getEnv("DATABASE_PORT")
	user := getEnv("DATABASE_USERNAME")
	password := getEnv("DATABASE_PASSWORD")
	dbname := getEnv("DATABASE_NAME")
	sslmode := getEnv("DATABASE_SSLMODE")

	return fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=%s sslmode=%s",
		host, port, user, password, dbname, sslmode,
	)
}

func getEnv(key string) string {
	value, exists := os.LookupEnv(key)
	if !exists {
		panic(fmt.Sprintf("required environment variable %s is not set", key))
	}
	return value
}
