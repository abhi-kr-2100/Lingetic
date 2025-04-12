package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"

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

func main() {
	if len(os.Args) != 2 {
		log.Fatal("Usage: worker <processor-name>")
	}

	processorName := os.Args[1]
	processor, err := getProcessor(processorName)
	if err != nil {
		log.Fatalf("Failed to get processor: %v", err)
	}
	defer processor.Close()

	// Connect to RabbitMQ
	conn, err := types.GetRabbitMQConnection()
	if err != nil {
		log.Fatalf("Failed to connect to RabbitMQ: %v", err)
	}
	defer conn.Close()

	ch, err := conn.Channel()
	if err != nil {
		log.Fatalf("Failed to open a channel: %v", err)
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
		log.Fatalf("Failed to declare queue: %v", err)
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
		log.Fatalf("Failed to register a consumer: %v", err)
	}

	stop := make(chan os.Signal, 1)
	signal.Notify(stop, syscall.SIGINT, syscall.SIGTERM)

	forever := make(chan bool)

	go func() {
		for d := range msgs {
			if err := processor.ProcessTask(d.Body); err != nil {
				log.Printf("Error processing task: %v", err)
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
