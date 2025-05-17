package testhelpers

import (
	"context"
	"database/sql"
	"fmt"
	"testing"
	"time"

	"github.com/docker/go-connections/nat"
	_ "github.com/lib/pq"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/wait"
)

// SetupPostgresTestDB starts a Postgres testcontainer and initializes the schema.
// Returns (*sql.DB, teardown function). Calls t.Fatalf on error.
func SetupPostgresTestDB(t *testing.T) (*sql.DB, func()) {
	ctx := context.Background()

	waitStrategy := wait.ForAll(
		wait.ForListeningPort("5432/tcp"),
		wait.ForSQL("5432/tcp", "postgres", func(host string, port nat.Port) string {
			return fmt.Sprintf(
				"postgres://testuser:testpass@%s:%s/testdb?sslmode=disable",
				host, port.Port(),
			)
		}).WithStartupTimeout(60*time.Second),
	)

	req := testcontainers.ContainerRequest{
		Image:        "postgres:17",
		ExposedPorts: []string{"5432/tcp"},
		Env: map[string]string{
			"POSTGRES_USER":     "testuser",
			"POSTGRES_PASSWORD": "testpass",
			"POSTGRES_DB":       "testdb",
		},
		WaitingFor: waitStrategy,
	}

	container, err := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
		ContainerRequest: req,
		Started:          true,
	})
	if err != nil {
		t.Fatalf("could not start container: %v", err)
	}

	host, err := container.Host(ctx)
	if err != nil {
		t.Fatalf("could not get container host: %v", err)
	}
	mappedPort, err := container.MappedPort(ctx, "5432/tcp")
	if err != nil {
		t.Fatalf("could not get mapped port: %v", err)
	}

	dsn := fmt.Sprintf(
		"postgres://testuser:testpass@%s:%s/testdb?sslmode=disable",
		host, mappedPort.Port(),
	)
	dbConn, err := sql.Open("postgres", dsn)
	if err != nil {
		t.Fatalf("could not open db: %v", err)
	}
	dbConn.SetMaxOpenConns(5)
	dbConn.SetMaxIdleConns(5)

	// Schema setup (identical to all current test setups)
	schema := `
	CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
	CREATE TABLE questions (
		id UUID PRIMARY KEY,
		question_type TEXT,
		language TEXT,
		difficulty SMALLINT,
		question_list_id UUID,
		question_type_specific_data JSONB
	);
	CREATE TABLE question_reviews (
		id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
		question_id UUID NOT NULL,
		user_id TEXT NOT NULL,
		language TEXT NOT NULL,
		repetitions INT NOT NULL DEFAULT 0,
		ease_factor REAL NOT NULL DEFAULT 2.5,
		interval INT NOT NULL DEFAULT 1,
		next_review_instant TIMESTAMP NOT NULL DEFAULT NOW(),
		UNIQUE(question_id, user_id)
	);
	`
	if _, err := dbConn.Exec(schema); err != nil {
		t.Fatalf("could not init schema: %v", err)
	}

	teardown := func() {
		dbConn.Close()
		container.Terminate(ctx)
	}
	return dbConn, teardown
}
