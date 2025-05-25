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

type SentenceReviewProcessingPayload struct {
	UserID     string        `json:"userId"`
	SentenceID string        `json:"sentenceId"`
	Status     AttemptStatus `json:"status"`
}

func (p *SentenceReviewProcessingPayload) Validate() error {
	if strings.TrimSpace(p.UserID) == "" {
		return errors.New("userId is blank")
	}
	if strings.TrimSpace(p.SentenceID) == "" {
		return errors.New("sentenceId is blank")
	}
	if p.Status != Success && p.Status != Failure {
		return errors.New("invalid status value")
	}
	return nil
}

type GenericTaskPayloadWrapper[T any] struct {
	Payload T `json:"payload"`
}
