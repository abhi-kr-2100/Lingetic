package db

import (
	"context"
	"database/sql"
	"errors"
	"testing"
	"time"

	_ "github.com/lib/pq"
	"munetmo.com/lingetic/workers/sentencereviewer/testhelpers"
)

// Define ErrNotFound at package level
var ErrNotFound = errors.New("not found")

func setupPostgresContainer(t *testing.T) (*SentenceRepository, *ReviewRepository, func()) {
	dbConn, teardown := testhelpers.SetupPostgresTestDB(t)
	err := testhelpers.ClearTestData(dbConn)
	if err != nil {
		t.Fatalf("Failed to clear test data: %v", err)
	}
	srepo := NewSentenceRepository(dbConn)
	rrepo := NewReviewRepository(dbConn, srepo)
	return srepo, rrepo, teardown
}

func insertSentence(db *SentenceRepository, s *Sentence) (string, error) {
	id := "550e8400-e29b-41d4-a716-446655440000" // Fixed UUID for testing
	_, err := db.DB.Exec(
		`INSERT INTO sentences (id, source_language, translation_language, source_text, translation_text)
		VALUES ($1, $2, $3, $4, $5)`,
		id, s.SourceLanguage, "Swedish", "Hello world", "Hola mundo",
	)
	return id, err
}

func insertReview(db *ReviewRepository, r *SentenceReview) error {
	_, err := db.DB.Exec(
		`INSERT INTO sentence_reviews
		(id, sentence_id, user_id, language, repetitions, ease_factor, interval, next_review_instant)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`,
		r.ID, r.SentenceID, r.UserID, r.Language, r.Repetitions, r.EaseFactor, r.Interval, r.NextReviewInstant,
	)
	return err
}

func TestSentenceRepository_GetByID(t *testing.T) {
	srepo, _, teardown := setupPostgresContainer(t)
	defer teardown()

	const (
		nonexistentID = "00000000-0000-0000-0000-000000000000"
		sourceLang    = "English"
		targetLang    = "Swedish"
	)

	t.Run("sentence does not exist", func(t *testing.T) {
		err := testhelpers.ClearTestData(srepo.DB)
		if err != nil {
			t.Fatalf("Failed to clear test data: %v", err)
		}

		_, err = srepo.GetByID(context.Background(), nonexistentID)
		if err == nil {
			t.Fatal("expected error for non-existent sentence, got nil")
		}
		if err == nil || !errors.Is(err, sql.ErrNoRows) {
			t.Fatalf("expected sql.ErrNoRows, got %v", err)
		}
	})

	t.Run("sentence exists", func(t *testing.T) {
		err := testhelpers.ClearTestData(srepo.DB)
		if err != nil {
			t.Fatalf("Failed to clear test data: %v", err)
		}

		sentence := &Sentence{
			SourceLanguage: sourceLang,
		}
		id, err := insertSentence(srepo, sentence)
		if err != nil {
			t.Fatalf("failed to insert sentence: %v", err)
		}

		fetched, err := srepo.GetByID(context.Background(), id)
		if err != nil {
			t.Fatalf("GetByID failed: %v", err)
		}
		if fetched.SourceLanguage != sourceLang {
			t.Fatalf("fetched sentence language mismatch: got %v want %v", fetched.SourceLanguage, sourceLang)
		}
		// Remove TranslationLanguage check since it's not part of the Sentence struct
	})

	t.Run("invalid id format", func(t *testing.T) {
		err := testhelpers.ClearTestData(srepo.DB)
		if err != nil {
			t.Fatalf("Failed to clear test data: %v", err)
		}

		_, err = srepo.GetByID(context.Background(), "invalid-id")
		if err == nil {
			t.Fatal("expected error for invalid id format, got nil")
		}
	})
}

func TestReviewRepository_GetReviewForSentenceOrCreateNew(t *testing.T) {
	srepo, rrepo, teardown := setupPostgresContainer(t)
	defer teardown()

	const (
		nonexistentID = "00000000-0000-0000-0000-000000000000"
		sentenceID    = "550e8400-e29b-41d4-a716-446655440000"
		userID        = "user123"
		anotherUserID = "user456"
		sourceLang    = "English"
	)

	t.Run("sentence does not exist", func(t *testing.T) {
		err := testhelpers.ClearTestData(srepo.DB)
		if err != nil {
			t.Fatalf("Failed to clear test data: %v", err)
		}

		_, err = rrepo.GetReviewForSentenceOrCreateNew(context.Background(), userID, nonexistentID)
		if err == nil {
			t.Fatal("expected error when creating/fetching review for non-existent sentence, got nil")
		}
	})

	t.Run("sentence exists, review does not exist", func(t *testing.T) {
		err := testhelpers.ClearTestData(srepo.DB)
		if err != nil {
			t.Fatalf("Failed to clear test data: %v", err)
		}

		sentence := &Sentence{
			SourceLanguage: sourceLang,
		}
		id, err := insertSentence(srepo, sentence)
		if err != nil {
			t.Fatalf("failed to insert sentence: %v", err)
		}

		review, err := rrepo.GetReviewForSentenceOrCreateNew(context.Background(), userID, id)
		if err != nil {
			t.Fatalf("unexpected error: %v", err)
		}
		if review.SentenceID != id || review.UserID != userID {
			t.Fatalf("review data mismatch: got %+v", review)
		}

		var count int
		err = rrepo.DB.QueryRow(
			"SELECT COUNT(*) FROM sentence_reviews WHERE sentence_id = $1 AND user_id = $2",
			id, userID,
		).Scan(&count)
		if err != nil {
			t.Fatalf("error counting reviews: %v", err)
		}
		if count != 1 {
			t.Fatalf("expected 1 review, got %d", count)
		}
	})

	t.Run("sentence exists, review exists", func(t *testing.T) {
		err := testhelpers.ClearTestData(srepo.DB)
		if err != nil {
			t.Fatalf("Failed to clear test data: %v", err)
		}

		sentence := &Sentence{
			SourceLanguage: sourceLang,
		}
		sentenceID, err := insertSentence(srepo, sentence)
		if err != nil {
			t.Fatalf("failed to insert sentence: %v", err)
		}

		review := &SentenceReview{
			ID:                "123e4567-e89b-12d3-a456-426614174000",
			SentenceID:        sentenceID,
			UserID:            anotherUserID,
			Language:          "English",
			Repetitions:       1,
			EaseFactor:        2.7,
			Interval:          3,
			NextReviewInstant: time.Now().Add(24 * time.Hour),
		}

		if err := insertReview(rrepo, review); err != nil {
			t.Fatalf("failed to insert review: %v", err)
		}

		retrieved, err := rrepo.GetReviewForSentenceOrCreateNew(context.Background(), anotherUserID, sentenceID)
		if err != nil {
			t.Fatalf("unexpected error: %v", err)
		}
		if retrieved.ID != review.ID || retrieved.UserID != anotherUserID {
			t.Fatalf("should fetch existing review for correct user: got %+v", retrieved)
		}

		var count int
		err = rrepo.DB.QueryRow(
			"SELECT COUNT(*) FROM sentence_reviews WHERE sentence_id = $1 AND user_id = $2",
			sentenceID, anotherUserID,
		).Scan(&count)
		if err != nil {
			t.Fatalf("error counting reviews: %v", err)
		}
		if count != 1 {
			t.Fatalf("expected 1 review, got %d", count)
		}
	})
}

func TestReviewRepository_Update(t *testing.T) {
	srepo, rrepo, teardown := setupPostgresContainer(t)
	defer teardown()

	err := testhelpers.ClearTestData(srepo.DB)
	if err != nil {
		t.Fatalf("Failed to clear test data: %v", err)
	}

	const (
		sourceLang = "English"
		userID     = "user123"
	)

	t.Run("update modifies review correctly", func(t *testing.T) {
		err := testhelpers.ClearTestData(srepo.DB)
		if err != nil {
			t.Fatalf("Failed to clear test data: %v", err)
		}

		sentence := &Sentence{
			SourceLanguage: sourceLang,
		}
		sentenceID, err := insertSentence(srepo, sentence)
		if err != nil {
			t.Fatalf("failed to insert sentence: %v", err)
		}

		review, err := rrepo.GetReviewForSentenceOrCreateNew(context.Background(), userID, sentenceID)
		if err != nil {
			t.Fatalf("fetch review failed: %v", err)
		}

		updatedReview := *review
		updatedReview.Repetitions = 5
		updatedReview.EaseFactor = 2.8
		updatedReview.Interval = 10
		updatedReview.NextReviewInstant = time.Now().Add(48 * time.Hour)

		err = rrepo.Update(context.Background(), &updatedReview)
		if err != nil {
			t.Fatalf("update failed: %v", err)
		}

		// Fetch the review again to verify changes
		fetched, err := rrepo.GetReviewForSentenceOrCreateNew(context.Background(), userID, sentenceID)
		if err != nil {
			t.Fatalf("fetch after update failed: %v", err)
		}

		if fetched.Repetitions != updatedReview.Repetitions {
			t.Fatalf("Repetitions did not match: got %v, want %v", fetched.Repetitions, updatedReview.Repetitions)
		}
		if fetched.EaseFactor != updatedReview.EaseFactor {
			t.Fatalf("EaseFactor did not match: got %v, want %v", fetched.EaseFactor, updatedReview.EaseFactor)
		}
		if fetched.Interval != updatedReview.Interval {
			t.Fatalf("Interval did not match: got %v, want %v", fetched.Interval, updatedReview.Interval)
		}
	})

	t.Run("update non-existent review", func(t *testing.T) {
		err := testhelpers.ClearTestData(srepo.DB)
		if err != nil {
			t.Fatalf("Failed to clear test data: %v", err)
		}

		nonExistentReview := &SentenceReview{
			ID:                "00000000-0000-0000-0000-000000000000",
			SentenceID:        "00000000-0000-0000-0000-000000000001",
			UserID:            userID,
			Language:          sourceLang,
			Repetitions:       1,
			EaseFactor:        2.5,
			Interval:          1,
			NextReviewInstant: time.Now(),
		}

		err = rrepo.Update(context.Background(), nonExistentReview)
		if err == nil {
			t.Fatal("expected error when updating non-existent review, got nil")
		}
	})
}
