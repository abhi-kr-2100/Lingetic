package types

import (
	"database/sql"
	"fmt"
	"log"
	"os"
	"time"

	_ "github.com/lib/pq"
	amqp "github.com/rabbitmq/amqp091-go"
)

// TaskProcessor interface that all task processors must implement
type TaskProcessor interface {
	// QueueName returns the name of the queue this processor handles
	QueueName() string
	// ProcessTask handles a single task message
	ProcessTask([]byte) error
	// Close cleans up any resources used by the processor
	Close() error
}

// GetRabbitMQConnection returns a connection to RabbitMQ using environment variables
func GetRabbitMQConnection() (*amqp.Connection, error) {
	host := getEnvironmentVariableOrDie("RABBITMQ_HOST")
	vhost := getEnvironmentVariableOrDie("RABBITMQ_VHOST")
	port := getEnvironmentVariableOrDie("RABBITMQ_PORT")
	user := getEnvironmentVariableOrDie("RABBITMQ_USERNAME")
	pass := getEnvironmentVariableOrDie("RABBITMQ_PASSWORD")

	connectionURL := fmt.Sprintf("amqp://%s:%s@%s:%s%s",
		user, pass, host, port, vhost)

	return amqp.Dial(connectionURL)
}

// GetDatabaseConnection returns a connection to PostgreSQL using environment variables
func GetDatabaseConnection() (*sql.DB, error) {
	dbUser := getEnvironmentVariableOrDie("DATABASE_USERNAME")
	dbPass := getEnvironmentVariableOrDie("DATABASE_PASSWORD")
	dbHost := getEnvironmentVariableOrDie("DATABASE_HOST")
	dbPort := getEnvironmentVariableOrDie("DATABASE_PORT")
	dbName := getEnvironmentVariableOrDie("DATABASE_NAME")
	sslMode := getEnvironmentVariableOrDie("DATABASE_SSLMODE")

	connStr := fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=%s",
		dbHost, dbPort, dbUser, dbPass, dbName, sslMode)
	db, err := sql.Open("postgres", connStr)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to database: %v", err)
	}

	// Verify connection is working
	if err := db.Ping(); err != nil {
		db.Close() // Clean up before returning error
		return nil, fmt.Errorf("failed to ping database: %v", err)
	}

	// Set connection pool parameters
	db.SetMaxOpenConns(25)
	db.SetMaxIdleConns(5)
	db.SetConnMaxLifetime(5 * time.Minute)

	return db, nil
}

func getEnvironmentVariableOrDie(key string) string {
	if value, exists := os.LookupEnv(key); exists {
		return value
	}
	log.Fatalf("Environment variable %s not set", key)
	return ""
}
