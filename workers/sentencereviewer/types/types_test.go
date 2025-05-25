package types

import (
	"errors"
	"testing"
)

func TestSentenceReviewProcessingPayload_Validate(t *testing.T) {
	tests := []struct {
		name    string
		payload SentenceReviewProcessingPayload
		wantErr bool
		errMsg  string
	}{
		{
			name: "valid payload with success status",
			payload: SentenceReviewProcessingPayload{
				UserID:     "user123",
				SentenceID: "sentence456",
				Status:     Success,
			},
			wantErr: false,
		},
		{
			name: "valid payload with failure status",
			payload: SentenceReviewProcessingPayload{
				UserID:     "user123",
				SentenceID: "sentence456",
				Status:     Failure,
			},
			wantErr: false,
		},
		{
			name: "empty user ID",
			payload: SentenceReviewProcessingPayload{
				UserID:     "",
				SentenceID: "sentence456",
				Status:     Success,
			},
			wantErr: true,
			errMsg:  "userId is blank",
		},
		{
			name: "empty sentence ID",
			payload: SentenceReviewProcessingPayload{
				UserID:     "user123",
				SentenceID: "",
				Status:     Success,
			},
			wantErr: true,
			errMsg:  "sentenceId is blank",
		},
		{
			name: "invalid status",
			payload: SentenceReviewProcessingPayload{
				UserID:     "user123",
				SentenceID: "sentence456",
				Status:     "invalid-status",
			},
			wantErr: true,
			errMsg:  "invalid status value",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			err := tt.payload.Validate()
			if tt.wantErr {
				if err == nil {
					t.Error("expected error, got nil")
				} else if !errors.Is(err, errors.New(tt.errMsg)) && err.Error() != tt.errMsg {
					t.Errorf("error message not as expected. got=%q, want contains %q", err.Error(), tt.errMsg)
				}
			} else if err != nil {
				t.Errorf("unexpected error: %v", err)
			}
		})
	}
}
