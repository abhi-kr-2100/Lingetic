package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/getsentry/sentry-go"

	"lingetic/workers/processors"
	"lingetic/workers/types"
)

// getProcessor returns the processor for the given task type
func getProcessor(task string) (types.TaskProcessor, error) {
	if task == "question-review" {
		return processors.NewQuestionReviewProcessor()
	}

	return nil, fmt.Errorf("unknown task type: %s", task)
}

func initSentry() error {
	return sentry.Init(sentry.ClientOptions{
		Dsn: "https://8ad8f8e91c359c460cc57f75645c92b0@o4508705106952192.ingest.de.sentry.io/4509141118222416",
	})
}

func main() {
	err := initSentry()
	if err != nil {
		log.Fatalf("sentry.Init: %s", err)
	}
	defer sentry.Flush(2 * time.Second)

	if len(os.Args) != 2 {
		log.Fatalf("worker started with wrong arguments: %v", os.Args)
	}

	processorName := os.Args[1]
	processor, err := getProcessor(processorName)
	if err != nil {
		log.Fatalf("failed to get processor: %v", err)
	}
	defer processor.Close()

	// Connect to RabbitMQ
	conn, err := types.GetRabbitMQConnection()
	if err != nil {
		log.Fatalf("failed to connect to RabbitMQ: %v", err)
	}
	defer conn.Close()

	ch, err := conn.Channel()
	if err != nil {
		log.Fatalf("failed to open a channel: %v", err)
	}
	defer ch.Close()

	// Declare the queue for this processor
	queueName := processor.QueueName()
	q, err := ch.QueueDeclare(
		queueName,
		true,  // durable
		false, // delete when unused
		false, // exclusive
		false, // no-wait
		nil,   // arguments
	)
	if err != nil {
		log.Fatalf("failed to declare queue: %v", err)
	}

	msgs, err := ch.Consume(
		q.Name,
		"",    // consumer
		true,  // auto-ack
		false, // exclusive
		false, // no-local
		false, // no-wait
		nil,   // args
	)
	if err != nil {
		log.Fatalf("failed to register a consumer: %v", err)
	}

	stop := make(chan os.Signal, 1)
	signal.Notify(stop, syscall.SIGINT, syscall.SIGTERM)

	forever := make(chan bool)

	go func() {
		for d := range msgs {
			if err := processor.ProcessTask(d.Body); err != nil {
				err := fmt.Errorf("failed to process task: %v", err)
				sentry.CaptureException(err)
				log.Print(err)
			}
		}
	}()

	log.Printf("[*] Using processor %s", processorName)
	log.Printf("[*] Waiting for messages in queue %s. To exit press CTRL+C", queueName)

	select {
	case <-stop:
		log.Println("Shutting down gracefully...")
	case <-forever:
	}
}
