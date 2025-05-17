package db

import (
	"context"
	"encoding/json"
	"testing"
	"time"

	_ "github.com/lib/pq"
	"munetmo.com/lingetic/workers/questionreviewer/testhelpers"
)

func setupPostgresContainer(t *testing.T) (*QuestionRepository, *ReviewRepository, func()) {
	dbConn, teardown := testhelpers.SetupPostgresTestDB(t)
	qrepo := &QuestionRepository{DB: dbConn}
	rrepo := &ReviewRepository{DB: dbConn, QuestionRepo: qrepo}
	return qrepo, rrepo, teardown
}

// Utility to insert a question
func insertQuestion(db *QuestionRepository, q Question) error {
	_, err := db.DB.Exec(
		`INSERT INTO questions (id,question_type,language,difficulty,question_list_id,question_type_specific_data)
		   VALUES ($1,$2,$3,$4,$5,$6)`,
		q.ID, q.QuestionType, q.Language, q.Difficulty, q.QuestionListID, q.QuestionTypeSpecificData,
	)
	return err
}

// Utility to insert a review
func insertReview(db *ReviewRepository, r QuestionReview) error {
	_, err := db.DB.Exec(
		`INSERT INTO question_reviews
			(id, question_id, user_id, language, repetitions, ease_factor, interval, next_review_instant)
		 VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`,
		r.ID, r.QuestionID, r.UserID, r.Language, r.Repetitions, r.EaseFactor, r.Interval, r.NextReviewInstant)
	return err
}

func TestQuestionRepository_GetQuestionByID(t *testing.T) {
	qrepo, _, teardown := setupPostgresContainer(t)
	defer teardown()

	const (
		nonexistentQID = "00000000-0000-0000-0000-000000000000"
		existingQID    = "ae7e2f70-c1c3-4aa2-ae8e-b7054d2ab7e8"
		existingListID = "7a2ec230-4f21-454c-9dfa-4c6f320f5ac6"
	)

	sampleQuestion := Question{
		ID:                       existingQID,
		QuestionType:             "multiple-choice",
		Language:                 "en",
		Difficulty:               2,
		QuestionListID:           existingListID,
		QuestionTypeSpecificData: json.RawMessage(`{"choices":["A","B","C"],"answer":"A"}`),
	}

	t.Run("question does not exist", func(t *testing.T) {
		_, err := qrepo.GetQuestionByID(context.Background(), nonexistentQID)
		if err == nil {
			t.Fatalf("expected error for non-existent question, got nil")
		}
	})

	t.Run("question exists", func(t *testing.T) {
		err := insertQuestion(qrepo, sampleQuestion)
		if err != nil {
			t.Fatalf("Failed to insert question: %v", err)
		}

		fetched, err := qrepo.GetQuestionByID(context.Background(), sampleQuestion.ID)
		if err != nil {
			t.Fatalf("GetQuestionByID failed: %v", err)
		}
		if fetched.ID != sampleQuestion.ID {
			t.Fatalf("fetched question id mismatch: got %v want %v", fetched.ID, sampleQuestion.ID)
		}
	})

	t.Run("question id is invalid", func(t *testing.T) {
		_, err := qrepo.GetQuestionByID(context.Background(), "invalid-id")
		if err == nil {
			t.Fatalf("expected error for invalid question id, got nil")
		}
	})
}

func TestReviewRepository_GetReviewForQuestionOrCreateNew(t *testing.T) {
	qrepo, rrepo, teardown := setupPostgresContainer(t)
	defer teardown()

	const (
		nonexistentQID = "00000000-0000-0000-0000-000000000000"
		existingQID    = "ae7e2f70-c1c3-4aa2-ae8e-b7054d2ab7e8"
		existingListID = "7a2ec230-4f21-454c-9dfa-4c6f320f5ac6"
		userID         = "user123"
		userIDAnother  = "user999"
	)

	sampleQuestion := Question{
		ID:                       existingQID,
		QuestionType:             "multiple-choice",
		Language:                 "en",
		Difficulty:               2,
		QuestionListID:           existingListID,
		QuestionTypeSpecificData: json.RawMessage(`{"choices":["A","B","C"],"answer":"A"}`),
	}

	t.Run("question does not exist", func(t *testing.T) {
		_, err := rrepo.GetReviewForQuestionOrCreateNew(context.Background(), userID, nonexistentQID)
		if err == nil {
			t.Fatalf("expected error when creating/fetching review for non-existent question, got nil")
		}
	})

	t.Run("question exists, review does not exist", func(t *testing.T) {
		err := insertQuestion(qrepo, sampleQuestion)
		if err != nil {
			t.Fatalf("Failed to insert question: %v", err)
		}

		review, err := rrepo.GetReviewForQuestionOrCreateNew(context.Background(), userID, sampleQuestion.ID)
		if err != nil {
			t.Fatalf("unexpected error: %v", err)
		}
		if review.QuestionID != sampleQuestion.ID || review.UserID != userID {
			t.Fatalf("review data mismatch: got %+v", review)
		}

		var count int
		rrepo.DB.QueryRow(`SELECT COUNT(*) FROM question_reviews WHERE question_id=$1 AND user_id=$2`, sampleQuestion.ID, userID).Scan(&count)
		if count != 1 {
			t.Fatalf("expected 1 review, got %d", count)
		}
	})

	t.Run("question exists, review exists", func(t *testing.T) {
		review := QuestionReview{
			ID:                "abcd1234-ab12-cd34-ef56-abcd1234ef56",
			QuestionID:        sampleQuestion.ID,
			UserID:            userIDAnother,
			Language:          sampleQuestion.Language,
			Repetitions:       1,
			EaseFactor:        2.7,
			Interval:          3,
			NextReviewInstant: time.Now().Add(24 * time.Hour),
		}
		if err := insertReview(rrepo, review); err != nil {
			t.Fatalf("failed to insert review: %v", err)
		}

		retrieved, err := rrepo.GetReviewForQuestionOrCreateNew(context.Background(), userIDAnother, sampleQuestion.ID)
		if err != nil {
			t.Fatalf("unexpected error: %v", err)
		}
		if retrieved.ID != review.ID || retrieved.UserID != userIDAnother {
			t.Fatalf("should fetch existing review for correct user: got %+v", retrieved)
		}

		var count int
		rrepo.DB.QueryRow(`SELECT COUNT(*) FROM question_reviews WHERE question_id=$1 AND user_id=$2`, sampleQuestion.ID, userIDAnother).Scan(&count)
		if count != 1 {
			t.Fatalf("expected 1 review, got %d", count)
		}
	})
}

func TestReviewRepository_Update(t *testing.T) {
	qrepo, rrepo, teardown := setupPostgresContainer(t)
	defer teardown()

	const (
		existingQID    = "ae7e2f70-c1c3-4aa2-ae8e-b7054d2ab7e8"
		existingListID = "7a2ec230-4f21-454c-9dfa-4c6f320f5ac6"
		userID         = "user123"
	)

	sampleQuestion := Question{
		ID:                       existingQID,
		QuestionType:             "multiple-choice",
		Language:                 "en",
		Difficulty:               2,
		QuestionListID:           existingListID,
		QuestionTypeSpecificData: json.RawMessage(`{"choices":["A","B","C"],"answer":"A"}`),
	}

	t.Run("update modifies review correctly", func(t *testing.T) {
		err := insertQuestion(qrepo, sampleQuestion)
		if err != nil {
			t.Fatalf("Failed to insert question: %v", err)
		}

		rev, err := rrepo.GetReviewForQuestionOrCreateNew(context.Background(), userID, sampleQuestion.ID)
		if err != nil {
			t.Fatalf("fetch review failed: %v", err)
		}

		modifiedReview := QuestionReview{
			ID:                rev.ID,
			QuestionID:        rev.QuestionID,
			UserID:            rev.UserID,
			Language:          rev.Language,
			Repetitions:       5,
			EaseFactor:        3.5,
			Interval:          25,
			NextReviewInstant: time.Now().Add(72 * time.Hour),
		}
		err = rrepo.Update(context.Background(), &modifiedReview)
		if err != nil {
			t.Fatalf("update failed: %v", err)
		}

		var gotReps int
		rrepo.DB.QueryRow(`SELECT repetitions FROM question_reviews WHERE id=$1`, rev.ID).Scan(&gotReps)
		if gotReps != 5 {
			t.Fatalf("expected repetitions=5, got %d", gotReps)
		}
	})
}
