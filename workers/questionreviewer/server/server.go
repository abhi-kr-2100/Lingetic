package server

import (
	"context"
	"encoding/json"
	"net/http"
	"time"

	"munetmo.com/lingetic/workers/questionreviewer/db"
	"munetmo.com/lingetic/workers/questionreviewer/types"
	"munetmo.com/lingetic/workers/questionreviewer/usecase"
)

type Server struct {
	SecretKey    string
	QuestionRepo *db.QuestionRepository
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
	var wrapper types.GenericTaskPayloadWrapper[types.QuestionReviewProcessingPayload]
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
	if err := usecase.ReviewQuestion(ctx, server.QuestionRepo, server.ReviewRepo, wrapper.Payload); err != nil {
		http.Error(writer, "Review failed: "+err.Error(), http.StatusBadRequest)
		return
	}

	writer.WriteHeader(http.StatusOK)
	writer.Write([]byte("Review processed successfully"))
}
