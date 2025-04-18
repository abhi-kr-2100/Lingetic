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
	protocol := GetEnvironmentVariableOrDie("RABBITMQ_PROTOCOL")
	host := GetEnvironmentVariableOrDie("RABBITMQ_HOST")
	vhost := GetEnvironmentVariableOrDie("RABBITMQ_VHOST")
	port := GetEnvironmentVariableOrDie("RABBITMQ_PORT")
	user := GetEnvironmentVariableOrDie("RABBITMQ_USERNAME")
	pass := GetEnvironmentVariableOrDie("RABBITMQ_PASSWORD")

	connectionURL := fmt.Sprintf("%s://%s:%s@%s:%s%s",
		protocol, user, pass, host, port, vhost)

	return amqp.Dial(connectionURL)
}

// GetDatabaseConnection returns a connection to PostgreSQL using environment variables
func GetDatabaseConnection() (*sql.DB, error) {
	dbUser := GetEnvironmentVariableOrDie("DATABASE_USERNAME")
	dbPass := GetEnvironmentVariableOrDie("DATABASE_PASSWORD")
	dbHost := GetEnvironmentVariableOrDie("DATABASE_HOST")
	dbPort := GetEnvironmentVariableOrDie("DATABASE_PORT")
	dbName := GetEnvironmentVariableOrDie("DATABASE_NAME")
	sslMode := GetEnvironmentVariableOrDie("DATABASE_SSLMODE")

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

func GetEnvironmentVariableOrDie(key string) string {
	if value, exists := os.LookupEnv(key); exists {
		return value
	}
	log.Fatalf("Environment variable %s not set", key)
	return ""
}
