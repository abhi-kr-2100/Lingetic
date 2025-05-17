package server

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/http/httptest"
	"testing"

	_ "github.com/lib/pq"
	"munetmo.com/lingetic/workers/questionreviewer/db"
	"munetmo.com/lingetic/workers/questionreviewer/testhelpers"
	"munetmo.com/lingetic/workers/questionreviewer/types"
)

// Helper to build a valid payload for testing
func validPayload(questionID string) []byte {
	payload := types.GenericTaskPayloadWrapper[types.QuestionReviewProcessingPayload]{
		Payload: types.QuestionReviewProcessingPayload{
			UserID:     "test-user1",
			QuestionID: questionID,
			Status:     types.Success,
		},
	}
	data, _ := json.Marshal(payload)
	return data
}

func setupServerTestEnv(t *testing.T) (*Server, func()) {
	dbConn, teardown := testhelpers.SetupPostgresTestDB(t)
	qrepo := &db.QuestionRepository{DB: dbConn}
	rrepo := &db.ReviewRepository{DB: dbConn, QuestionRepo: qrepo}
	secret := "mysecret"
	srv := &Server{
		SecretKey:    secret,
		QuestionRepo: qrepo,
		ReviewRepo:   rrepo,
	}
	return srv, teardown
}

// Helper to set up a DB with or without an inserted question
func setupWithQuestion(t *testing.T, insertQuestion bool) (*Server, func(), string) {
	srv, teardown := setupServerTestEnv(t)
	questionID := "60e805aa-1e91-43c3-b09c-2321ef16af9b"
	if insertQuestion {
		_, err := srv.QuestionRepo.DB.Exec(`INSERT INTO questions (id,question_type,language,difficulty,question_list_id,question_type_specific_data)
			VALUES ($1,'short','ru',2,$2,$3)`, questionID, "88a7b9c6-7a08-46fe-82af-3084e3a5a7e6", []byte(`{"prompt":"What's up?"}`))
		if err != nil {
			t.Fatalf("insert question: %v", err)
		}
	}
	return srv, teardown, questionID
}

// Case: No secret key provided (should fail)
func TestPostReview_NoSecretKey(t *testing.T) {
	srv, teardown, questionID := setupWithQuestion(t, true)
	defer teardown()

	req := httptest.NewRequest("POST", "/", bytes.NewReader(validPayload(questionID)))
	w := httptest.NewRecorder()
	srv.ServeHTTP(w, req)
	resp := w.Result()
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusUnauthorized {
		t.Errorf("Expected 401 Unauthorized without secret key, got %d", resp.StatusCode)
	}
}

// Case: Provided secret key is wrong (should fail)
func TestPostReview_WrongSecretKey(t *testing.T) {
	srv, teardown, questionID := setupWithQuestion(t, true)
	defer teardown()

	req := httptest.NewRequest("POST", "/?key=wrong", bytes.NewReader(validPayload(questionID)))
	w := httptest.NewRecorder()
	srv.ServeHTTP(w, req)
	if w.Result().StatusCode != http.StatusUnauthorized {
		t.Errorf("Expected unauthorized status for wrong secret key, got %d", w.Result().StatusCode)
	}
}

// Case: Correct secret key (happy path)
func TestPostReview_SecretKeyCorrect(t *testing.T) {
	srv, teardown, questionID := setupWithQuestion(t, true)
	defer teardown()

	url := fmt.Sprintf("/?key=%s", srv.SecretKey)
	req := httptest.NewRequest("POST", url, bytes.NewReader(validPayload(questionID)))
	w := httptest.NewRecorder()
	srv.ServeHTTP(w, req)
	resp := w.Result()
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		bb, _ := io.ReadAll(resp.Body)
		t.Fatalf("unexpected code: %d, body: %s", resp.StatusCode, string(bb))
	}
}

// Case: Method is not POST (should get 404 or appropriate error)
func TestPostReview_MethodNotPOST(t *testing.T) {
	srv, teardown, _ := setupWithQuestion(t, true)
	defer teardown()

	req := httptest.NewRequest("GET", "/?key="+srv.SecretKey, nil)
	w := httptest.NewRecorder()
	srv.ServeHTTP(w, req)
	if w.Result().StatusCode != http.StatusNotFound {
		t.Errorf("Expected 404 for GET, got %d", w.Result().StatusCode)
	}
}

// Case: Path is not "/" (should get 404)
func TestPostReview_PathNotSlash(t *testing.T) {
	srv, teardown, _ := setupWithQuestion(t, true)
	defer teardown()

	req := httptest.NewRequest("POST", "/not-slash?key="+srv.SecretKey, nil)
	w := httptest.NewRecorder()
	srv.ServeHTTP(w, req)
	if w.Result().StatusCode != http.StatusNotFound {
		t.Errorf("Expected 404 for POST /not-slash, got %d", w.Result().StatusCode)
	}
}

// Case: POST, but path not "/" (should get 404)
func TestPostReview_PostWrongPath(t *testing.T) {
	srv, teardown, _ := setupWithQuestion(t, true)
	defer teardown()

	req := httptest.NewRequest("POST", "/some/other/path?key="+srv.SecretKey, nil)
	w := httptest.NewRecorder()
	srv.ServeHTTP(w, req)
	if w.Result().StatusCode != http.StatusNotFound {
		t.Errorf("Expected 404 for POST /some/other/path, got %d", w.Result().StatusCode)
	}
}

// Case: "/" but not POST (should get 404)
func TestPostReview_PathIsSlashMethodNotPOST(t *testing.T) {
	srv, teardown, _ := setupWithQuestion(t, true)
	defer teardown()

	req := httptest.NewRequest("GET", "/?key="+srv.SecretKey, nil)
	w := httptest.NewRecorder()
	srv.ServeHTTP(w, req)
	if w.Result().StatusCode != http.StatusNotFound {
		t.Errorf("Expected 404 for GET /, got %d", w.Result().StatusCode)
	}
}

// Case: Payload format is incorrect (malformed JSON)
func TestPostReview_PayloadMalformedJSON(t *testing.T) {
	srv, teardown, _ := setupWithQuestion(t, true)
	defer teardown()

	url := fmt.Sprintf("/?key=%s", srv.SecretKey)
	req := httptest.NewRequest("POST", url, bytes.NewReader([]byte(`not-json`)))
	w := httptest.NewRecorder()
	srv.ServeHTTP(w, req)
	resp := w.Result()
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusBadRequest {
		t.Errorf("Expected 400 for malformed JSON, got %d", resp.StatusCode)
	}
}

// Case: Payload format is correct but question doesn't exist (should fail 400/404)
func TestPostReview_CorrectPayload_QuestionDoesNotExist(t *testing.T) {
	srv, teardown, _ := setupWithQuestion(t, false) // Don't insert question
	defer teardown()

	url := fmt.Sprintf("/?key=%s", srv.SecretKey)
	req := httptest.NewRequest("POST", url, bytes.NewReader(validPayload("non-existent-question-id")))
	w := httptest.NewRecorder()
	srv.ServeHTTP(w, req)
	resp := w.Result()
	defer resp.Body.Close()
	if resp.StatusCode == http.StatusOK {
		t.Errorf("Expected error for non-existent question, got 200 OK")
	}
}

// Case: Payload format is correct, but payload itself invalid (missing fields, bad enum, etc.)
func TestPostReview_CorrectPayload_BadPayload(t *testing.T) {
	srv, teardown, _ := setupWithQuestion(t, true)
	defer teardown()

	payload := map[string]any{
		// purposely omitting required fields
		"bad": "data",
	}
	body, _ := json.Marshal(payload)
	url := fmt.Sprintf("/?key=%s", srv.SecretKey)
	req := httptest.NewRequest("POST", url, bytes.NewReader(body))
	w := httptest.NewRecorder()
	srv.ServeHTTP(w, req)
	resp := w.Result()
	defer resp.Body.Close()

	// Assuming API returns BadRequest for a payload that fails validation
	if resp.StatusCode != http.StatusBadRequest {
		t.Errorf("Expected 400 for missing/strange JSON fields, got %d", resp.StatusCode)
	}
}

// Case: Question exists, review doesn't exist (should succeed)
func TestPostReview_QuestionExists_ReviewDoesNotExist(t *testing.T) {
	srv, teardown, questionID := setupWithQuestion(t, true)
	defer teardown()

	url := fmt.Sprintf("/?key=%s", srv.SecretKey)
	req := httptest.NewRequest("POST", url, bytes.NewReader(validPayload(questionID)))
	w := httptest.NewRecorder()
	srv.ServeHTTP(w, req)
	resp := w.Result()
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		bb, _ := io.ReadAll(resp.Body)
		t.Errorf("Expected 200 OK for fresh review, got %d; body: %s", resp.StatusCode, string(bb))
	}
}

// Case: Question and review both exist (should handle idempotency/duplicate gracefully)
func TestPostReview_QuestionAndReviewExist(t *testing.T) {
	srv, teardown, questionID := setupWithQuestion(t, true)
	defer teardown()

	url := fmt.Sprintf("/?key=%s", srv.SecretKey)
	body := validPayload(questionID)

	// First submission -- create the review
	req1 := httptest.NewRequest("POST", url, bytes.NewReader(body))
	w1 := httptest.NewRecorder()
	srv.ServeHTTP(w1, req1)
	if w1.Result().StatusCode != http.StatusOK {
		t.Fatalf("initial review creation should succeed")
	}

	// Second submission -- review already exists
	req2 := httptest.NewRequest("POST", url, bytes.NewReader(body))
	w2 := httptest.NewRecorder()
	srv.ServeHTTP(w2, req2)
	resp2 := w2.Result()
	defer resp2.Body.Close()
	// Accept either 200 (idempotent) or a 409/duplicate flag depending on your actual logic
	if resp2.StatusCode != http.StatusOK {
		t.Errorf("Expected idempotent success or duplicate accept on existing review, got %d", resp2.StatusCode)
	}
}
