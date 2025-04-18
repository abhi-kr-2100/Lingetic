package main

import (
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"time"

	"github.com/getsentry/sentry-go"

	"lingetic/workers/processors"
	"lingetic/workers/types"
)

func getProcessor(task string) (types.TaskProcessor, error) {
	if task == "question-review" {
		return processors.NewQuestionReviewProcessor()
	}

	return nil, fmt.Errorf("unknown task type: %s", task)
}

func initSentry() {
	environment := types.GetEnvironmentVariableOrDie("ENVIRONMENT")

	err := sentry.Init(sentry.ClientOptions{
		Dsn:         "https://8ad8f8e91c359c460cc57f75645c92b0@o4508705106952192.ingest.de.sentry.io/4509141118222416",
		Environment: environment,
	})

	if err != nil {
		log.Fatalf("sentry.Init: %s", err)
	}
}

func main() {
	initSentry()
	defer sentry.Flush(2 * time.Second)

	if len(os.Args) < 2 {
		sentry.CaptureMessage("No processor name provided")
		log.Println("No processor name provided")
		return
	}
	processorName := os.Args[1]
	processor, err := getProcessor(processorName)
	if err != nil {
		sentry.CaptureException(err)
		log.Printf("failed to get processor: %v", err)
		return
	}
	defer processor.Close()

	setupHttpServer(processor)

	addr := ":8080"
	log.Printf("[*] HTTP server listening on %s with processor %s", addr, processorName)
	if err := http.ListenAndServe(addr, nil); err != nil {
		sentry.CaptureException(err)
		log.Printf("failed to start HTTP server: %v", err)
		return
	}
}

func setupHttpServer(processor types.TaskProcessor) {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			http.Error(w, "Only POST is allowed", http.StatusMethodNotAllowed)
			return
		}

		body, err := io.ReadAll(r.Body)
		if err != nil {
			sentry.CaptureException(err)
			http.Error(w, "Failed to read request body", http.StatusBadRequest)
			return
		}
		defer r.Body.Close()

		if err := processor.ProcessTask(body); err != nil {
			err := fmt.Errorf("body: '%s': failed to process task: %v", string(body), err)
			sentry.CaptureException(err)
			http.Error(w, "Failed to process task", http.StatusInternalServerError)
			return
		}

		w.WriteHeader(http.StatusOK)
		w.Write([]byte("processed"))
	})
}
