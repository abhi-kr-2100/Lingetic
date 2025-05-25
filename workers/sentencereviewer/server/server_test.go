package server_test

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"munetmo.com/lingetic/workers/sentencereviewer/db"
	server "munetmo.com/lingetic/workers/sentencereviewer/server"
	"munetmo.com/lingetic/workers/sentencereviewer/testhelpers"
	"munetmo.com/lingetic/workers/sentencereviewer/types"
)

func setupTestServer(t *testing.T) (*server.Server, func()) {
	dbConn, teardown := testhelpers.SetupPostgresTestDB(t)
	srepo := db.NewSentenceRepository(dbConn)
	rrepo := db.NewReviewRepository(dbConn, srepo)

	secretKey := "test-secret-key"

	srv := &server.Server{
		SecretKey:    secretKey,
		SentenceRepo: srepo,
		ReviewRepo:   rrepo,
	}

	return srv, teardown
}

func insertTestSentence(t *testing.T, srepo *db.SentenceRepository, lang string) string {
	id := "550e8400-e29b-41d4-a716-446655440000"
	_, err := srepo.DB.Exec(
		`INSERT INTO sentences (id, source_language, translation_language, source_text, translation_text)
		VALUES ($1, $2, $3, $4, $5)`,
		id, lang, "Swedish", "Test sentence", "Oración de prueba",
	)
	if err != nil {
		t.Fatalf("failed to insert test sentence: %v", err)
	}
	return id
}

func createTestRequest(t *testing.T, payload types.SentenceReviewProcessingPayload) *http.Request {
	secretKey := "test-secret-key"

	wrapper := types.GenericTaskPayloadWrapper[types.SentenceReviewProcessingPayload]{
		Payload: payload,
	}
	body, err := json.Marshal(wrapper)
	if err != nil {
		t.Fatalf("failed to marshal wrapper: %v", err)
	}
	req := httptest.NewRequest("POST", "/?key="+secretKey, bytes.NewBuffer(body))
	req.Header.Set("Content-Type", "application/json")
	return req
}

func TestServer_ServeHTTP(t *testing.T) {
	tests := []struct {
		name         string
		setupTest    func(t *testing.T, srepo *db.SentenceRepository) (string, types.SentenceReviewProcessingPayload)
		setupRequest func(t *testing.T, payload types.SentenceReviewProcessingPayload, key string) *http.Request
		expectedCode int
	}{
		{
			name: "valid request with success status",
			setupTest: func(t *testing.T, srepo *db.SentenceRepository) (string, types.SentenceReviewProcessingPayload) {
				sentenceID := insertTestSentence(t, srepo, "English")
				payload := types.SentenceReviewProcessingPayload{
					UserID:     "user1",
					SentenceID: sentenceID,
					Status:     types.Success,
				}
				return sentenceID, payload
			},
			setupRequest: func(t *testing.T, payload types.SentenceReviewProcessingPayload, key string) *http.Request {
				return createTestRequest(t, payload)
			},
			expectedCode: http.StatusOK,
		},
		{
			name: "valid request with failure status",
			setupTest: func(t *testing.T, srepo *db.SentenceRepository) (string, types.SentenceReviewProcessingPayload) {
				sentenceID := insertTestSentence(t, srepo, "English")
				payload := types.SentenceReviewProcessingPayload{
					UserID:     "user1",
					SentenceID: sentenceID,
					Status:     types.Failure,
				}
				return sentenceID, payload
			},
			setupRequest: func(t *testing.T, payload types.SentenceReviewProcessingPayload, key string) *http.Request {
				return createTestRequest(t, payload)
			},
			expectedCode: http.StatusOK,
		},
		{
			name: "invalid http method",
			setupTest: func(t *testing.T, srepo *db.SentenceRepository) (string, types.SentenceReviewProcessingPayload) {
				return "", types.SentenceReviewProcessingPayload{}
			},
			setupRequest: func(t *testing.T, _ types.SentenceReviewProcessingPayload, key string) *http.Request {
				return httptest.NewRequest("GET", "/?key="+key, nil)
			},
			expectedCode: http.StatusNotFound,
		},
		{
			name: "missing key",
			setupTest: func(t *testing.T, srepo *db.SentenceRepository) (string, types.SentenceReviewProcessingPayload) {
				sentenceID := insertTestSentence(t, srepo, "English")
				payload := types.SentenceReviewProcessingPayload{
					UserID:     "user1",
					SentenceID: sentenceID,
					Status:     types.Success,
				}
				return sentenceID, payload
			},
			setupRequest: func(t *testing.T, payload types.SentenceReviewProcessingPayload, _ string) *http.Request {
				wrapper := types.GenericTaskPayloadWrapper[types.SentenceReviewProcessingPayload]{
					Payload: payload,
				}
				body, _ := json.Marshal(wrapper)
				req := httptest.NewRequest("POST", "/", bytes.NewBuffer(body))
				req.Header.Set("Content-Type", "application/json")
				return req
			},
			expectedCode: http.StatusUnauthorized,
		},
		{
			name: "invalid json payload",
			setupTest: func(t *testing.T, srepo *db.SentenceRepository) (string, types.SentenceReviewProcessingPayload) {
				return "", types.SentenceReviewProcessingPayload{}
			},
			setupRequest: func(t *testing.T, _ types.SentenceReviewProcessingPayload, key string) *http.Request {
				req := httptest.NewRequest("POST", "/?key="+key, bytes.NewBufferString("invalid-json"))
				req.Header.Set("Content-Type", "application/json")
				return req
			},
			expectedCode: http.StatusBadRequest,
		},
		{
			name: "invalid payload validation",
			setupTest: func(t *testing.T, srepo *db.SentenceRepository) (string, types.SentenceReviewProcessingPayload) {
				return "", types.SentenceReviewProcessingPayload{}
			},
			setupRequest: func(t *testing.T, _ types.SentenceReviewProcessingPayload, key string) *http.Request {
				wrapper := types.GenericTaskPayloadWrapper[types.SentenceReviewProcessingPayload]{
					Payload: types.SentenceReviewProcessingPayload{
						// Missing required fields
					},
				}
				body, _ := json.Marshal(wrapper)
				req := httptest.NewRequest("POST", "/?key="+key, bytes.NewBuffer(body))
				req.Header.Set("Content-Type", "application/json")
				return req
			},
			expectedCode: http.StatusBadRequest,
		},
		{
			name: "non-existent sentence",
			setupTest: func(t *testing.T, srepo *db.SentenceRepository) (string, types.SentenceReviewProcessingPayload) {
				return "non-existent-sentence", types.SentenceReviewProcessingPayload{
					UserID:     "user1",
					SentenceID: "non-existent-sentence",
					Status:     types.Success,
				}
			},
			setupRequest: func(t *testing.T, payload types.SentenceReviewProcessingPayload, key string) *http.Request {
				wrapper := types.GenericTaskPayloadWrapper[types.SentenceReviewProcessingPayload]{
					Payload: payload,
				}
				body, _ := json.Marshal(wrapper)
				req := httptest.NewRequest("POST", "/?key="+key, bytes.NewBuffer(body))
				req.Header.Set("Content-Type", "application/json")
				return req
			},
			expectedCode: http.StatusBadRequest,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			srv, teardown := setupTestServer(t)
			defer teardown()

			var payload types.SentenceReviewProcessingPayload
			if tt.setupTest != nil {
				_, payload = tt.setupTest(t, srv.SentenceRepo)
			}

			req := tt.setupRequest(t, payload, srv.SecretKey)
			rr := httptest.NewRecorder()

			srv.ServeHTTP(rr, req)

			if rr.Code != tt.expectedCode {
				t.Errorf("expected status code %d, got %d", tt.expectedCode, rr.Code)
			}
		})
	}
}

func TestServer_NoSecretKey(t *testing.T) {
	srv := &server.Server{
		SecretKey: "", // No secret key set
	}

	req := httptest.NewRequest("POST", "/?key=any-key", nil)
	rr := httptest.NewRecorder()

	srv.ServeHTTP(rr, req)

	if rr.Code != http.StatusInternalServerError {
		t.Errorf("expected status code %d, got %d", http.StatusInternalServerError, rr.Code)
	}

	if !strings.Contains(rr.Body.String(), "Secret key not set in environment") {
		t.Errorf("expected body to contain %q, got %q", "Secret key not set in environment", rr.Body.String())
	}
}

func TestServer_ReviewUpdatesDatabase(t *testing.T) {
	srv, teardown := setupTestServer(t)
	defer teardown()

	// Insert a test sentence
	sentenceID := insertTestSentence(t, srv.SentenceRepo, "English")

	// First review - success
	payload := types.SentenceReviewProcessingPayload{
		UserID:     "user1",
		SentenceID: sentenceID,
		Status:     types.Success,
	}

	// Send the request
	wrapper := types.GenericTaskPayloadWrapper[types.SentenceReviewProcessingPayload]{
		Payload: payload,
	}
	body, _ := json.Marshal(wrapper)
	req := httptest.NewRequest("POST", "/?key="+srv.SecretKey, bytes.NewBuffer(body))
	req.Header.Set("Content-Type", "application/json")
	rr := httptest.NewRecorder()

	srv.ServeHTTP(rr, req)

	// Check response
	if rr.Code != http.StatusOK {
		t.Errorf("expected status code %d, got %d", http.StatusOK, rr.Code)
	}

	bodyStr := rr.Body.String()
	if !strings.Contains(bodyStr, "Review processed successfully") {
		t.Error("expected body to contain 'Review processed successfully'")
	}

	// Verify database state
	review, err := srv.ReviewRepo.GetReviewForSentenceOrCreateNew(context.Background(), "user1", sentenceID)
	if err != nil {
		t.Fatalf("failed to get review: %v", err)
	}
	if review.Repetitions != 1 {
		t.Errorf("expected 1 repetition, got %d", review.Repetitions)
	}
	if review.Interval != 1 {
		t.Errorf("expected interval 1, got %d", review.Interval)
	}
	if review.EaseFactor < 2.6 || review.EaseFactor > 2.6001 {
		t.Errorf("expected ease factor ~2.6, got %f", review.EaseFactor)
	}
}
