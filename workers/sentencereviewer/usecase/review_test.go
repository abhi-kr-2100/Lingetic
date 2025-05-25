package usecase_test

import (
	"context"
	"strings"
	"testing"

	"munetmo.com/lingetic/workers/sentencereviewer/db"
	"munetmo.com/lingetic/workers/sentencereviewer/testhelpers"
	"munetmo.com/lingetic/workers/sentencereviewer/types"
	"munetmo.com/lingetic/workers/sentencereviewer/usecase"
)

func setupTestDB(t *testing.T) (*db.SentenceRepository, *db.ReviewRepository, func()) {
	dbConn, teardown := testhelpers.SetupPostgresTestDB(t)
	srepo := db.NewSentenceRepository(dbConn)
	rrepo := db.NewReviewRepository(dbConn, srepo)
	return srepo, rrepo, teardown
}

func insertTestSentence(srepo *db.SentenceRepository, lang string) (string, error) {
	sentence := &db.Sentence{
		SourceLanguage: lang,
	}
	id := "550e8400-e29b-41d4-a716-446655440000"
	_, err := srepo.DB.Exec(
		`INSERT INTO sentences (id, source_language, translation_language, source_text, translation_text)
		VALUES ($1, $2, $3, $4, $5)`,
		id, sentence.SourceLanguage, "Swedish", "Test sentence", "Oración de prueba",
	)
	return id, err
}

// TestReviewEdgeCases tests various edge cases in the review process
func TestReviewEdgeCases(t *testing.T) {
	ctx := context.Background()
	srepo, rrepo, teardown := setupTestDB(t)
	defer teardown()

	t.Run("multiple consecutive failures", func(t *testing.T) {
		err := testhelpers.ClearTestData(srepo.DB)
		if err != nil {
			t.Fatalf("Failed to clear test data: %v", err)
		}
		sentenceID, err := insertTestSentence(srepo, "English")
		if err != nil {
			t.Fatalf("failed to insert test sentence: %v", err)
		}

		// First failure
		err = usecase.ReviewSentence(ctx, srepo, rrepo, types.SentenceReviewProcessingPayload{
			UserID:     "user1",
			SentenceID: sentenceID,
			Status:     types.Failure,
		})
		if err != nil {
			t.Fatalf("first review failed: %v", err)
		}

		review, err := rrepo.GetReviewForSentenceOrCreateNew(ctx, "user1", sentenceID)
		if err != nil {
			t.Fatalf("failed to get review: %v", err)
		}
		if review.Repetitions != 0 {
			t.Errorf("expected 0 repetitions, got %d", review.Repetitions)
		}
		if review.Interval != 0 {
			t.Errorf("expected 0 interval, got %d", review.Interval)
		}

		// Second failure
		err = usecase.ReviewSentence(ctx, srepo, rrepo, types.SentenceReviewProcessingPayload{
			UserID:     "user1",
			SentenceID: sentenceID,
			Status:     types.Failure,
		})
		if err != nil {
			t.Fatalf("second review failed: %v", err)
		}

		review, err = rrepo.GetReviewForSentenceOrCreateNew(ctx, "user1", sentenceID)
		if err != nil {
			t.Fatalf("failed to get review: %v", err)
		}
		if review.Repetitions != 0 {
			t.Errorf("expected 0 repetitions after second failure, got %d", review.Repetitions)
		}
		if review.Interval != 0 {
			t.Errorf("expected 0 interval after second failure, got %d", review.Interval)
		}
	})

	t.Run("alternating success and failure", func(t *testing.T) {
		err := testhelpers.ClearTestData(srepo.DB)
		if err != nil {
			t.Fatalf("Failed to clear test data: %v", err)
		}
		sentenceID, err := insertTestSentence(srepo, "English")
		if err != nil {
			t.Fatalf("failed to insert test sentence: %v", err)
		}

		// Success
		err = usecase.ReviewSentence(ctx, srepo, rrepo, types.SentenceReviewProcessingPayload{
			UserID:     "user2",
			SentenceID: sentenceID,
			Status:     types.Success,
		})
		if err != nil {
			t.Fatalf("first review failed: %v", err)
		}

		review, err := rrepo.GetReviewForSentenceOrCreateNew(ctx, "user2", sentenceID)
		if err != nil {
			t.Fatalf("failed to get review: %v", err)
		}
		initialEase := review.EaseFactor

		// Failure
		err = usecase.ReviewSentence(ctx, srepo, rrepo, types.SentenceReviewProcessingPayload{
			UserID:     "user2",
			SentenceID: sentenceID,
			Status:     types.Failure,
		})
		if err != nil {
			t.Fatalf("second review failed: %v", err)
		}

		review, err = rrepo.GetReviewForSentenceOrCreateNew(ctx, "user2", sentenceID)
		if err != nil {
			t.Fatalf("failed to get review: %v", err)
		}
		if review.Repetitions != 0 {
			t.Errorf("expected 0 repetitions after failure, got %d", review.Repetitions)
		}
		if review.Interval != 0 {
			t.Errorf("expected 0 interval after failure, got %d", review.Interval)
		}
		if review.EaseFactor >= initialEase {
			t.Errorf("ease factor should decrease after failure, was %f, now %f", initialEase, review.EaseFactor)
		}
	})
}

// TestReviewProcess tests the main review workflow including success and failure cases
// It verifies that the review process updates the sentence review correctly based on the attempt status
// and handles edge cases like non-existent sentences
func TestReviewProcess(t *testing.T) {
	ctx := context.Background()
	srepo, rrepo, teardown := setupTestDB(t)
	defer teardown()

	tests := []struct {
		name         string
		setup        func() (types.SentenceReviewProcessingPayload, error)
		wantErr      bool
		errContains  string
		verifyResult func(t *testing.T, payload types.SentenceReviewProcessingPayload, rrepo *db.ReviewRepository)
	}{
		{
			name: "successful review with success status",
			setup: func() (types.SentenceReviewProcessingPayload, error) {
				sentenceID, err := insertTestSentence(srepo, "English")
				if err != nil {
					t.Fatalf("failed to insert test sentence: %v", err)
				}

				return types.SentenceReviewProcessingPayload{
					UserID:     "user1",
					SentenceID: sentenceID,
					Status:     types.Success,
				}, nil
			},
			wantErr: false,
			verifyResult: func(t *testing.T, payload types.SentenceReviewProcessingPayload, rrepo *db.ReviewRepository) {
				review, err := rrepo.GetReviewForSentenceOrCreateNew(context.Background(), payload.UserID, payload.SentenceID)
				if err != nil {
					t.Errorf("failed to get review: %v", err)
				}
				if review.Repetitions != 1 {
					t.Errorf("expected 1 repetition, got %d", review.Repetitions)
				}
				if review.Interval != 1 {
					t.Errorf("expected interval 1, got %d", review.Interval)
				}
				if review.EaseFactor < 2.6 || review.EaseFactor > 2.6001 {
					t.Errorf("expected ease factor 2.6, got %f", review.EaseFactor)
				}
			},
		},
		{
			name: "successful review with failure status",
			setup: func() (types.SentenceReviewProcessingPayload, error) {
				sentenceID, err := insertTestSentence(srepo, "English")
				if err != nil {
					t.Fatalf("failed to insert test sentence: %v", err)
				}

				return types.SentenceReviewProcessingPayload{
					UserID:     "user1",
					SentenceID: sentenceID,
					Status:     types.Failure,
				}, nil
			},
			wantErr: false,
			verifyResult: func(t *testing.T, payload types.SentenceReviewProcessingPayload, rrepo *db.ReviewRepository) {
				review, err := rrepo.GetReviewForSentenceOrCreateNew(context.Background(), payload.UserID, payload.SentenceID)
				if err != nil {
					t.Errorf("failed to get review: %v", err)
				}
				if review.Repetitions != 0 {
					t.Errorf("expected 0 repetitions, got %d", review.Repetitions)
				}
				if review.Interval != 0 {
					t.Errorf("expected interval 0, got %d", review.Interval)
				}
				if review.EaseFactor < 1.7 || review.EaseFactor > 1.7001 {
					t.Errorf("expected ease factor 1.7, got %f", review.EaseFactor)
				}
			},
		},
		{
			name: "repeated successful reviews increase interval",
			setup: func() (types.SentenceReviewProcessingPayload, error) {
				sentenceID, err := insertTestSentence(srepo, "English")
				if err != nil {
					t.Fatalf("failed to insert test sentence: %v", err)
				}

				// First review
				err = usecase.ReviewSentence(ctx, srepo, rrepo, types.SentenceReviewProcessingPayload{
					UserID:     "user1",
					SentenceID: sentenceID,
					Status:     types.Success,
				})
				if err != nil {
					t.Fatalf("first review failed: %v", err)
				}

				return types.SentenceReviewProcessingPayload{
					UserID:     "user1",
					SentenceID: sentenceID,
					Status:     types.Success, // Second review
				}, nil
			},
			wantErr: false,
			verifyResult: func(t *testing.T, payload types.SentenceReviewProcessingPayload, rrepo *db.ReviewRepository) {
				review, err := rrepo.GetReviewForSentenceOrCreateNew(context.Background(), payload.UserID, payload.SentenceID)
				if err != nil {
					t.Errorf("failed to get review: %v", err)
				}
				if review.Repetitions != 2 {
					t.Errorf("expected 2 repetitions, got %d", review.Repetitions)
				}
				if review.Interval != 6 {
					t.Errorf("expected interval 6, got %d", review.Interval)
				} // Should be 6 days after second success
			},
		},
		{
			name: "non-existent sentence",
			setup: func() (types.SentenceReviewProcessingPayload, error) {
				return types.SentenceReviewProcessingPayload{
					UserID:     "user1",
					SentenceID: "non-existent-sentence",
					Status:     types.Success,
				}, nil
			},
			wantErr:     true,
			errContains: "review row error",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			err := testhelpers.ClearTestData(srepo.DB)
			if err != nil {
				t.Fatalf("Failed to clear test data: %v", err)
			}
			payload, err := tt.setup()
			if err != nil {
				t.Fatalf("test setup failed: %v", err)
			}

			err = usecase.ReviewSentence(ctx, srepo, rrepo, payload)

			if tt.wantErr {
				if err == nil {
					t.Error("expected error but got none")
				} else if tt.errContains != "" && !strings.Contains(err.Error(), tt.errContains) {
					t.Errorf("error message does not contain %q: %v", tt.errContains, err)
				}
			} else {
				if err != nil {
					t.Fatalf("unexpected error: %v", err)
				}
				if tt.verifyResult != nil {
					tt.verifyResult(t, payload, rrepo)
				}
			}
		})
	}
}
