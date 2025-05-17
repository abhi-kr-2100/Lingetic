package usecase

import (
	"context"
	"encoding/json"
	"testing"
	"time"

	"github.com/google/uuid"
	"munetmo.com/lingetic/workers/questionreviewer/db"
	"munetmo.com/lingetic/workers/questionreviewer/testhelpers"
	"munetmo.com/lingetic/workers/questionreviewer/types"
)

func setupContainerAndRepos(t *testing.T) (*db.QuestionRepository, *db.ReviewRepository, func()) {
	dbConn, teardown := testhelpers.SetupPostgresTestDB(t)
	qr := &db.QuestionRepository{DB: dbConn}
	rr := &db.ReviewRepository{DB: dbConn, QuestionRepo: qr}
	return qr, rr, teardown
}

func insertTestQuestion(t *testing.T, qrepo *db.QuestionRepository, qid string) {
	question := db.Question{
		ID:                       qid,
		QuestionType:             "fill-in-the-blank",
		Language:                 "de",
		Difficulty:               1,
		QuestionListID:           uuid.NewString(),
		QuestionTypeSpecificData: json.RawMessage(`{"sentence":"Ich bin ___","answer":"müde"}`),
	}
	_, err := qrepo.DB.Exec(`INSERT INTO questions (id, question_type, language, difficulty, question_list_id, question_type_specific_data)
		VALUES ($1,$2,$3,$4,$5,$6)`, question.ID, question.QuestionType, question.Language, question.Difficulty, question.QuestionListID, question.QuestionTypeSpecificData)
	if err != nil {
		t.Fatalf("insert question: %v", err)
	}
}

// --- 1. Successful review on new question
func TestReview_SuccessOnNewQuestion(t *testing.T) {
	qrepo, rrepo, teardown := setupContainerAndRepos(t)
	defer teardown()
	questionID := uuid.NewString()
	userID := "user1"
	insertTestQuestion(t, qrepo, questionID)
	payload := types.QuestionReviewProcessingPayload{
		UserID:     userID,
		QuestionID: questionID,
		Status:     types.Success,
	}

	ctx := context.Background()
	if err := ReviewQuestion(ctx, qrepo, rrepo, payload); err != nil {
		t.Fatalf("ReviewQuestion failed: %v", err)
	}
	row := qrepo.DB.QueryRow(`SELECT repetitions, interval, ease_factor FROM question_reviews WHERE question_id = $1 AND user_id = $2`, questionID, userID)
	var reps, interval int
	var ef float32
	if err := row.Scan(&reps, &interval, &ef); err != nil {
		t.Fatalf("query review: %v", err)
	}
	if reps != 1 || interval != 1 || ef < 1.3 {
		t.Errorf("expected reps=1/interval=1/ef>=1.3 got %d/%d/%.2f", reps, interval, ef)
	}
}

// --- 2. Successful review on mature question
func TestReview_SuccessOnMatureQuestion(t *testing.T) {
	qrepo, rrepo, teardown := setupContainerAndRepos(t)
	defer teardown()
	questionID := uuid.NewString()
	userID := "user2"
	insertTestQuestion(t, qrepo, questionID)
	const matureReps = 10
	const matureInterval = 30
	const matureEF = 2.5
	reviewID := uuid.NewString()
	_, err := qrepo.DB.Exec(`INSERT INTO question_reviews (id, question_id, user_id, repetitions, interval, ease_factor, next_review_instant, language)
		VALUES ($1,$2,$3,$4,$5,$6,$7,$8)`, reviewID, questionID, userID, matureReps, matureInterval, matureEF, time.Now(), "de")
	if err != nil {
		t.Fatalf("insert review row: %v", err)
	}

	payload := types.QuestionReviewProcessingPayload{
		UserID:     userID,
		QuestionID: questionID,
		Status:     types.Success,
	}
	ctx := context.Background()
	if err := ReviewQuestion(ctx, qrepo, rrepo, payload); err != nil {
		t.Fatalf("ReviewQuestion failed: %v", err)
	}
	row := qrepo.DB.QueryRow(`SELECT repetitions, interval, ease_factor FROM question_reviews WHERE question_id = $1 AND user_id = $2`, questionID, userID)
	var reps, interval int
	var ef float32
	if err := row.Scan(&reps, &interval, &ef); err != nil {
		t.Fatalf("query review: %v", err)
	}
	if reps != matureReps+1 {
		t.Errorf("expected repetitions increment, got %d", reps)
	}
	if interval <= matureInterval {
		t.Errorf("interval did not increase, got %d", interval)
	}
	if ef < 1.3 {
		t.Errorf("ease_factor clamped incorrectly: got %f", ef)
	}
}

// --- 3. Failure review on new question
func TestReview_FailureOnNewQuestion(t *testing.T) {
	qrepo, rrepo, teardown := setupContainerAndRepos(t)
	defer teardown()
	questionID := uuid.NewString()
	userID := "user3"
	insertTestQuestion(t, qrepo, questionID)
	payload := types.QuestionReviewProcessingPayload{
		UserID:     userID,
		QuestionID: questionID,
		Status:     types.Failure,
	}
	ctx := context.Background()
	if err := ReviewQuestion(ctx, qrepo, rrepo, payload); err != nil {
		t.Fatalf("ReviewQuestion failed: %v", err)
	}
	row := qrepo.DB.QueryRow(`SELECT repetitions, interval, ease_factor FROM question_reviews WHERE question_id = $1 AND user_id = $2`, questionID, userID)
	var reps, interval int
	var ef float32
	if err := row.Scan(&reps, &interval, &ef); err != nil {
		t.Fatalf("query review: %v", err)
	}
	if reps != 0 || interval != 1 || ef < 1.3 {
		t.Errorf("expected reset: reps=0/interval=1/ef>=1.3, got %d/%d/%.2f", reps, interval, ef)
	}
}

// --- 4. Failure review on mature question
func TestReview_FailureOnMatureQuestion(t *testing.T) {
	qrepo, rrepo, teardown := setupContainerAndRepos(t)
	defer teardown()
	questionID := uuid.NewString()
	userID := "user4"
	insertTestQuestion(t, qrepo, questionID)
	reviewID := uuid.NewString()
	_, err := qrepo.DB.Exec(`INSERT INTO question_reviews (id, question_id, user_id, repetitions, interval, ease_factor, next_review_instant, language)
		VALUES ($1,$2,$3,10,30,2.5,$4,$5)`, reviewID, questionID, userID, time.Now(), "de")
	if err != nil {
		t.Fatalf("insert review row: %v", err)
	}
	payload := types.QuestionReviewProcessingPayload{
		UserID:     userID,
		QuestionID: questionID,
		Status:     types.Failure,
	}
	ctx := context.Background()
	if err := ReviewQuestion(ctx, qrepo, rrepo, payload); err != nil {
		t.Fatalf("ReviewQuestion failed: %v", err)
	}
	row := qrepo.DB.QueryRow(`SELECT repetitions, interval, ease_factor FROM question_reviews WHERE question_id = $1 AND user_id = $2`, questionID, userID)
	var reps, interval int
	var ef float32
	if err := row.Scan(&reps, &interval, &ef); err != nil {
		t.Fatalf("query review: %v", err)
	}
	if reps != 0 || interval != 1 || ef < 1.3 {
		t.Errorf("expected mature reset: reps=0/interval=1/ef>=1.3, got %d/%d/%.2f", reps, interval, ef)
	}
}

// --- 5a. Success on semi-mature question
func TestReview_SuccessOnSemiMatureQuestion(t *testing.T) {
	qrepo, rrepo, teardown := setupContainerAndRepos(t)
	defer teardown()
	questionID := uuid.NewString()
	userID := "user5"
	insertTestQuestion(t, qrepo, questionID)
	reviewID := uuid.NewString()
	_, err := qrepo.DB.Exec(`INSERT INTO question_reviews (id, question_id, user_id, repetitions, interval, ease_factor, next_review_instant, language)
		VALUES ($1,$2,$3,2,6,2.0,$4,$5)`, reviewID, questionID, userID, time.Now(), "de")
	if err != nil {
		t.Fatalf("insert review row: %v", err)
	}
	ctx := context.Background()
	payload := types.QuestionReviewProcessingPayload{
		UserID:     userID,
		QuestionID: questionID,
		Status:     types.Success,
	}
	if err := ReviewQuestion(ctx, qrepo, rrepo, payload); err != nil {
		t.Fatalf("ReviewQuestion (success) failed: %v", err)
	}
	row := qrepo.DB.QueryRow(`SELECT repetitions, interval, ease_factor FROM question_reviews WHERE question_id = $1 AND user_id = $2`, questionID, userID)
	var reps, interval int
	var ef float32
	if err := row.Scan(&reps, &interval, &ef); err != nil {
		t.Fatalf("query review (success): %v", err)
	}
	if reps != 3 || interval <= 6 || ef < 1.3 {
		t.Errorf("expected reps=3, interval > 6, ef>=1.3, got %d/%d/%.2f", reps, interval, ef)
	}
}

// --- 5b. Failure on semi-mature question
func TestReview_FailureOnSemiMatureQuestion(t *testing.T) {
	qrepo, rrepo, teardown := setupContainerAndRepos(t)
	defer teardown()
	questionID := uuid.NewString()
	userID := "user5f"
	insertTestQuestion(t, qrepo, questionID)
	reviewID := uuid.NewString()
	_, err := qrepo.DB.Exec(`INSERT INTO question_reviews (id, question_id, user_id, repetitions, interval, ease_factor, next_review_instant, language)
		VALUES ($1,$2,$3,2,6,2.0,$4,$5)`, reviewID, questionID, userID, time.Now(), "de")
	if err != nil {
		t.Fatalf("insert review row: %v", err)
	}
	ctx := context.Background()
	payload := types.QuestionReviewProcessingPayload{
		UserID:     userID,
		QuestionID: questionID,
		Status:     types.Failure,
	}
	if err := ReviewQuestion(ctx, qrepo, rrepo, payload); err != nil {
		t.Fatalf("ReviewQuestion (failure) failed: %v", err)
	}
	row := qrepo.DB.QueryRow(`SELECT repetitions, interval, ease_factor FROM question_reviews WHERE question_id = $1 AND user_id = $2`, questionID, userID)
	var reps, interval int
	var ef float32
	if err := row.Scan(&reps, &interval, &ef); err != nil {
		t.Fatalf("query review (failure): %v", err)
	}
	if reps != 0 || interval != 1 || ef < 1.3 {
		t.Errorf("after failure: reps=0/interval=1/ef>=1.3, got %d/%d/%.2f", reps, interval, ef)
	}
}

// --- 6. Review attempt on non-existing question
func TestReview_AttemptOnNonexistentQuestion(t *testing.T) {
	qrepo, rrepo, teardown := setupContainerAndRepos(t)
	defer teardown()
	userID := "user6"
	questionID := uuid.NewString() // Doesn't exist in questions table

	payload := types.QuestionReviewProcessingPayload{
		UserID:     userID,
		QuestionID: questionID,
		Status:     types.Success,
	}
	ctx := context.Background()
	err := ReviewQuestion(ctx, qrepo, rrepo, payload)
	if err == nil {
		t.Fatalf("expected error on review of non-existent question but got nil")
	}
}

// --- 7. Invalid Attempt status (should be error)
func TestReview_InvalidAttemptStatus_ReturnsError(t *testing.T) {
	qrepo, rrepo, teardown := setupContainerAndRepos(t)
	defer teardown()
	questionID := uuid.NewString()
	userID := "user_invalid"
	insertTestQuestion(t, qrepo, questionID)
	payload := types.QuestionReviewProcessingPayload{
		UserID:     userID,
		QuestionID: questionID,
		Status:     types.AttemptStatus("NotAStatus"),
	}
	ctx := context.Background()
	err := ReviewQuestion(ctx, qrepo, rrepo, payload)
	if err == nil {
		t.Fatalf("expected error with invalid AttemptStatus, but got nil")
	}
}

// Legacy test converted to use UUID
func TestReviewQuestion_FullCycle(t *testing.T) {
	qrepo, rrepo, teardown := setupContainerAndRepos(t)
	defer teardown()

	questionID := uuid.NewString()
	userID := "some-user-42"
	insertTestQuestion(t, qrepo, questionID)

	payload := types.QuestionReviewProcessingPayload{
		UserID:     userID,
		QuestionID: questionID,
		Status:     types.Success,
	}
	ctx := context.Background()
	if err := ReviewQuestion(ctx, qrepo, rrepo, payload); err != nil {
		t.Fatalf("ReviewQuestion failed: %v", err)
	}

	// Now update payload to indicate a failure; should reset repetition/interval
	payload.Status = types.Failure
	if err := ReviewQuestion(ctx, qrepo, rrepo, payload); err != nil {
		t.Fatalf("ReviewQuestion (to failure) failed: %v", err)
	}

	// Check DB state
	row := qrepo.DB.QueryRow(`SELECT repetitions, ease_factor FROM question_reviews WHERE question_id = $1 AND user_id = $2`, questionID, payload.UserID)
	var reps int
	var ef float32
	if err := row.Scan(&reps, &ef); err != nil {
		t.Fatalf("scan review row: %v", err)
	}
	if reps != 0 {
		t.Errorf("expected reps=0 after failure, got %d", reps)
	}
	if ef < 1.3 {
		t.Errorf("expected min ease_factor (>=1.3), got %.2f", ef)
	}
}
