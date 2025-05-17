package types

import (
	"testing"
)

func TestQuestionReviewProcessingPayload_Validate(t *testing.T) {
	tests := []struct {
		name    string
		payload QuestionReviewProcessingPayload
		wantErr bool
	}{
		{
			name: "Valid Success",
			payload: QuestionReviewProcessingPayload{
				UserID:     "user1",
				QuestionID: "q1",
				Status:     Success,
			},
			wantErr: false,
		},
		{
			name: "Valid Failure",
			payload: QuestionReviewProcessingPayload{
				UserID:     "user1",
				QuestionID: "q2",
				Status:     Failure,
			},
			wantErr: false,
		},
		{
			name: "Blank UserID",
			payload: QuestionReviewProcessingPayload{
				UserID:     "",
				QuestionID: "q3",
				Status:     Success,
			},
			wantErr: true,
		},
		{
			name: "Blank QuestionID",
			payload: QuestionReviewProcessingPayload{
				UserID:     "user2",
				QuestionID: " ",
				Status:     Failure,
			},
			wantErr: true,
		},
		{
			name: "Invalid Status",
			payload: QuestionReviewProcessingPayload{
				UserID:     "user2",
				QuestionID: "q4",
				Status:     "Bloop",
			},
			wantErr: true,
		},
	}
	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			err := tc.payload.Validate()
			if (err != nil) != tc.wantErr {
				t.Errorf("Validate() error = %v, wantErr %v", err, tc.wantErr)
			}
		})
	}
}
