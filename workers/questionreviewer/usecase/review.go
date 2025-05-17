package usecase

import (
	"context"
	"fmt"
	"time"

	"munetmo.com/lingetic/workers/questionreviewer/db"
	"munetmo.com/lingetic/workers/questionreviewer/types"
)

/**
 * Constants and helper for SuperMemo-2 spaced repetition algorithm.
 */
const (
	initialEaseFactor = 2.5
	minimumEaseFactor = 1.3
)

func getQualityFromStatus(status types.AttemptStatus) (int, error) {
	switch status {
	case types.Success:
		return 5, nil
	case types.Failure:
		return 0, nil
	}

	return 0, fmt.Errorf("invalid status: %v", status)
}

func updateReviewFieldsFromQuality(review *db.QuestionReview, quality int) *db.QuestionReview {
	next := *review

	if quality < 3 {
		next.Repetitions = 0
		next.Interval = 1
	} else {
		next.Repetitions = review.Repetitions + 1
		switch next.Repetitions {
		case 1:
			next.Interval = 1
		case 2:
			next.Interval = 6
		default:
			next.Interval = int(float32(review.Interval) * review.EaseFactor)
		}
	}

	// Update ease factor as in SuperMemo-2
	next.EaseFactor = review.EaseFactor + (0.1 - float32(5-quality)*(0.08+float32(5-quality)*0.02))
	if next.EaseFactor < minimumEaseFactor {
		next.EaseFactor = minimumEaseFactor
	}
	next.NextReviewInstant = time.Now().AddDate(0, 0, next.Interval)
	return &next
}

func ReviewQuestion(
	ctx context.Context,
	qrepo *db.QuestionRepository,
	rrepo *db.ReviewRepository,
	payload types.QuestionReviewProcessingPayload,
) error {
	// Find or insert review row, now grabs all review fields
	review, err := rrepo.GetReviewForQuestionOrCreateNew(ctx, payload.UserID, payload.QuestionID)
	if err != nil {
		return fmt.Errorf("review row error: %w", err)
	}

	quality, err := getQualityFromStatus(payload.Status)
	if err != nil {
		return fmt.Errorf("invalid attempt status: %w", err)
	}

	updated := updateReviewFieldsFromQuality(review, quality)

	// Push the update into the DB
	err = rrepo.Update(
		ctx,
		updated,
	)
	if err != nil {
		return fmt.Errorf("update review error: %w", err)
	}

	return nil
}
