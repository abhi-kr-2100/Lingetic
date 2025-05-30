package server

import (
	"context"
	"encoding/json"
	"log"
	"net/http"
	"time"

	"munetmo.com/lingetic/workers/sentencereviewer/db"
	"munetmo.com/lingetic/workers/sentencereviewer/types"
	"munetmo.com/lingetic/workers/sentencereviewer/usecase"
)

type Server struct {
	SecretKey    string
	SentenceRepo *db.SentenceRepository
	ReviewRepo   *db.ReviewRepository
}

func (server *Server) ServeHTTP(writer http.ResponseWriter, request *http.Request) {
	if request.Method != http.MethodPost || request.URL.Path != "/" {
		http.Error(writer, "Not found", http.StatusNotFound)
		return
	}

	key := request.URL.Query().Get("key")
	if server.SecretKey == "" {
		http.Error(writer, "Secret key not set in environment", http.StatusInternalServerError)
		return
	}
	if key != server.SecretKey {
		http.Error(writer, "Invalid key", http.StatusUnauthorized)
		return
	}

	defer request.Body.Close()
	var wrapper types.GenericTaskPayloadWrapper[types.SentenceReviewProcessingPayload]
	decoder := json.NewDecoder(request.Body)
	if err := decoder.Decode(&wrapper); err != nil {
		http.Error(writer, "Invalid payload format: "+err.Error(), http.StatusBadRequest)
		return
	}
	if err := wrapper.Payload.Validate(); err != nil {
		http.Error(writer, "Payload validation error: "+err.Error(), http.StatusBadRequest)
		return
	}

	ctx, cancel := context.WithTimeout(request.Context(), 10*time.Second)
	defer cancel()
	if err := usecase.ReviewSentence(ctx, server.SentenceRepo, server.ReviewRepo, wrapper.Payload); err != nil {
		log.Printf("ERROR: Failed to process review for sentence %s, user %s: %v",
			wrapper.Payload.SentenceID, wrapper.Payload.UserID, err)
		http.Error(writer, "Review failed: "+err.Error(), http.StatusBadRequest)
		return
	}

	writer.WriteHeader(http.StatusOK)
	writer.Write([]byte("Review processed successfully"))
}
