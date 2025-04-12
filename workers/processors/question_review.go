package processors

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"math"
	"time"

	"lingetic/workers/types"

	"github.com/google/uuid"
)

// AttemptStatus represents the possible statuses of a question attempt
type AttemptStatus string

const (
	Success AttemptStatus = "Success"
	Failure AttemptStatus = "Failure"
)

// Constants matching Java implementation
const (
	maxRepetitionsValue = 1000
	maxEaseFactorValue  = 5.0
	maxIntervalValue    = 365 * 10 // 10 years in days
)

// QuestionReviewProcessingPayload matches the Java QuestionReviewProcessingPayload record
type QuestionReviewProcessingPayload struct {
	UserID     string        `json:"userId"`
	QuestionID string        `json:"questionId"`
	Status     AttemptStatus `json:"status"`
}

// Task matches the Java Task class structure
type Task struct {
	ID      string                          `json:"id"`
	Payload QuestionReviewProcessingPayload `json:"payload"`
}

type QuestionReview struct {
	repetitions       int
	easeFactor        float64
	interval          int
	nextReviewInstant time.Time
}

// QuestionReviewProcessor handles question review processing tasks
type QuestionReviewProcessor struct {
	db *sql.DB
}

// NewQuestionReviewProcessor creates a new processor with database connection
func NewQuestionReviewProcessor() (*QuestionReviewProcessor, error) {
	db, err := types.GetDatabaseConnection()
	if err != nil {
		return nil, fmt.Errorf("failed to get database connection: %v", err)
	}

	return &QuestionReviewProcessor{db: db}, nil
}

func (p *QuestionReviewProcessor) Close() error {
	if p.db != nil {
		return p.db.Close()
	}
	return nil
}

func (p *QuestionReviewProcessor) QueueName() string {
	return "QUESTION_REVIEW_PROCESSING_QUEUE"
}

func (p *QuestionReviewProcessor) updateReview(userID string, questionID string, quality int) error {
	// There's a race condition here where the review might be updated by another
	// process while we're reading it. The probability of this happening is low,
	// and in case it does happen, it's not a big deal. A missed update will not
	// impact user experience significantly.
	var review QuestionReview
	err := p.db.QueryRow(`
		SELECT repetitions, ease_factor, interval, next_review_instant
		FROM question_reviews
		WHERE user_id = $1 AND question_id = $2::uuid`,
		userID, questionID).Scan(&review.repetitions, &review.easeFactor, &review.interval, &review.nextReviewInstant)

	if err == sql.ErrNoRows {
		// Need to fetch the language for the question
		var language string
		langErr := p.db.QueryRow(
			`SELECT language FROM questions WHERE id = $1::uuid`, questionID,
		).Scan(&language)
		if langErr != nil {
			return fmt.Errorf("failed to fetch language for question %s: %v", questionID, langErr)
		}

		review = QuestionReview{
			repetitions:       0,
			easeFactor:        2.5,
			interval:          0,
			nextReviewInstant: time.Now(),
		}
		id := uuid.NewString()
		_, insErr := p.db.Exec(`
			INSERT INTO question_reviews (
				id, question_id, user_id, language, repetitions, ease_factor, interval, next_review_instant
			) VALUES (
				$1::uuid, $2::uuid, $3, $4, $5, $6, $7, $8
			)`,
			id, questionID, userID, language, review.repetitions, review.easeFactor, review.interval, review.nextReviewInstant)
		if insErr != nil {
			return fmt.Errorf("failed to insert new review: %v", insErr)
		}
	} else if err != nil {
		return fmt.Errorf("failed to get review: %v", err)
	}

	if quality < 3 {
		review.repetitions = 0
		review.interval = 0
	} else {
		review.repetitions = int(math.Min(float64(review.repetitions+1), maxRepetitionsValue))

		if review.repetitions == 1 {
			review.interval = 1
		} else if review.repetitions == 2 {
			review.interval = 6
		} else {
			review.interval = int(math.Min(float64(int(math.Round(float64(review.interval)*review.easeFactor))), maxIntervalValue))
		}
	}

	review.easeFactor = math.Min(
		maxEaseFactorValue,
		math.Max(1.3, review.easeFactor+0.1-float64(5-quality)*(0.08+float64(5-quality)*0.02)),
	)

	review.nextReviewInstant = time.Now().AddDate(0, 0, review.interval)

	_, err = p.db.Exec(`
		UPDATE question_reviews
		SET repetitions = $1,
			ease_factor = $2,
			interval = $3,
			next_review_instant = $4
		WHERE user_id = $5 AND question_id = $6::uuid`,
		review.repetitions,
		review.easeFactor,
		review.interval,
		review.nextReviewInstant,
		userID,
		questionID)

	if err != nil {
		return fmt.Errorf("failed to update review in database: %v", err)
	}

	return nil
}

func (p *QuestionReviewProcessor) ProcessTask(data []byte) error {
	var task Task
	if err := json.Unmarshal(data, &task); err != nil {
		return fmt.Errorf("failed to unmarshal task data: %v", err)
	}

	log.Printf("Received task: %s", task.ID)
	log.Printf("User ID: %s", task.Payload.UserID)
	log.Printf("Question ID: %s", task.Payload.QuestionID)
	log.Printf("Status: %s", task.Payload.Status)

	// Convert status to quality score (0-5)
	quality := 0
	if task.Payload.Status == Success {
		quality = 5
	} else if task.Payload.Status == Failure {
		quality = 0
	} else {
		return fmt.Errorf("unknown status: %s", task.Payload.Status)
	}

	if err := p.updateReview(task.Payload.UserID, task.Payload.QuestionID, quality); err != nil {
		return fmt.Errorf("failed to update review: %v", err)
	}

	log.Printf("Successfully updated review for task: %s", task.ID)
	log.Printf("-------------------")

	return nil
}
