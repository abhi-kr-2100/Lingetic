package usecase

import (
	"context"
	"fmt"
	"math"
	"time"

	"munetmo.com/lingetic/workers/sentencereviewer/db"
	"munetmo.com/lingetic/workers/sentencereviewer/types"
)

const (
	initialEaseFactor    = 2.5
	minimumEaseFactor    = 1.3
	maxRepetitions       = 1000
	maxEaseFactor        = 5.0
	maxInterval          = 365 * 10 // 10 years in days
	maxReviewInstantDays = 365 * 10 // 10 years in days
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

func updateReviewFieldsFromQuality(review *db.SentenceReview, quality int) *db.SentenceReview {
	if quality < 0 || quality > 5 {
		// This should never happen as the status is validated earlier
		panic("quality must be between 0 and 5")
	}

	next := *review

	if quality < 3 {
		next.Repetitions = 0
		next.Interval = 0
	} else {
		next.Repetitions = min(review.Repetitions+1, maxRepetitions)

		switch next.Repetitions {
		case 1:
			next.Interval = 1
		case 2:
			next.Interval = 6
		default:
			interval := int(math.Round(float64(review.Interval) * review.EaseFactor))
			next.Interval = min(interval, maxInterval)
		}
	}

	ef := review.EaseFactor + (0.1 - float64(5-quality)*(0.08+float64(5-quality)*0.02))
	next.EaseFactor = max(minimumEaseFactor, min(ef, maxEaseFactor))
	next.NextReviewInstant = time.Now().AddDate(0, 0, next.Interval)

	return &next
}

func ReviewSentence(
	ctx context.Context,
	srepo *db.SentenceRepository,
	rrepo *db.ReviewRepository,
	payload types.SentenceReviewProcessingPayload,
) error {
	review, err := rrepo.GetReviewForSentenceOrCreateNew(ctx, payload.UserID, payload.SentenceID)
	if err != nil {
		return fmt.Errorf("review row error: %w", err)
	}

	quality, err := getQualityFromStatus(payload.Status)
	if err != nil {
		return fmt.Errorf("invalid attempt status: %w", err)
	}

	updated := updateReviewFieldsFromQuality(review, quality)

	err = rrepo.Update(
		ctx,
		updated,
	)
	if err != nil {
		return fmt.Errorf("update review error: %w", err)
	}

	return nil
}
