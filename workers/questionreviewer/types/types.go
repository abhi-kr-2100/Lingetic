package types

import (
	"errors"
	"strings"
)

type AttemptStatus string

const (
	Success AttemptStatus = "Success"
	Failure AttemptStatus = "Failure"
)

type QuestionReviewProcessingPayload struct {
	UserID     string        `json:"userId"`
	QuestionID string        `json:"questionId"`
	Status     AttemptStatus `json:"status"`
}

func (p *QuestionReviewProcessingPayload) Validate() error {
	if strings.TrimSpace(p.UserID) == "" {
		return errors.New("userId is blank")
	}
	if strings.TrimSpace(p.QuestionID) == "" {
		return errors.New("questionId is blank")
	}
	if p.Status != Success && p.Status != Failure {
		return errors.New("invalid status value")
	}
	return nil
}

type GenericTaskPayloadWrapper[T any] struct {
	Payload T `json:"payload"`
}
