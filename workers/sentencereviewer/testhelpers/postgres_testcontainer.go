package testhelpers

import (
	"context"
	"database/sql"
	"fmt"
	"testing"
	"time"

	_ "github.com/lib/pq"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/wait"
)

// SetupPostgresTestDB starts a Postgres testcontainer and initializes the schema.
// Returns (*sql.DB, teardown function). Calls t.Fatalf on error.
func SetupPostgresTestDB(t *testing.T) (*sql.DB, func()) {
	ctx := context.Background()

	req := testcontainers.ContainerRequest{
		Image:        "postgres:17",
		ExposedPorts: []string{"5432/tcp"},
		Env: map[string]string{
			"POSTGRES_USER":     "testuser",
			"POSTGRES_PASSWORD": "testpass",
			"POSTGRES_DB":       "testdb",
		},
		WaitingFor: wait.ForListeningPort("5432/tcp").
			WithStartupTimeout(60 * time.Second).
			WithPollInterval(1 * time.Second),
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

	// Create tables directly instead of using migrations
	_, err = dbConn.Exec(`
		CREATE TABLE IF NOT EXISTS languages (
			name TEXT PRIMARY KEY
		);

		-- Insert some common languages
		INSERT INTO languages (name) VALUES
			('English'),
			('Turkish'),
			('Swedish'),
			('Japanese'),
			('JapaneseModifiedHepburn')
		ON CONFLICT (name) DO NOTHING;

		CREATE TABLE IF NOT EXISTS sentences (
			id UUID PRIMARY KEY,
			source_language TEXT NOT NULL REFERENCES languages(name),
			source_text TEXT NOT NULL,
			translation_language TEXT NOT NULL REFERENCES languages(name),
			translation_text TEXT NOT NULL,
			difficulty INT DEFAULT 1
		);

		CREATE TABLE IF NOT EXISTS sentence_reviews (
			id UUID PRIMARY KEY,
			sentence_id UUID NOT NULL REFERENCES sentences(id) ON DELETE CASCADE,
			user_id TEXT NOT NULL,
			language TEXT NOT NULL CHECK (language in ('English', 'Turkish')),
			repetitions SMALLINT NOT NULL DEFAULT 0,
			ease_factor REAL NOT NULL DEFAULT 2.5 CHECK (ease_factor >= 1.29),
			"interval" SMALLINT NOT NULL DEFAULT 0 CHECK (interval >= 0),
			next_review_instant TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
			UNIQUE (sentence_id, user_id)
		);
	`)

	if err != nil {
		dbConn.Close()
		container.Terminate(ctx)
		t.Fatalf("could not initialize database schema: %v", err)
	}

	teardown := func() {
		dbConn.Close()
		container.Terminate(ctx)
	}

	return dbConn, teardown
}

func ClearTestData(db *sql.DB) error {
	tx, err := db.Begin()
	if err != nil {
		return err
	}

	_, err = tx.Exec("DELETE FROM sentence_reviews")
	if err != nil {
		tx.Rollback()
		return err
	}

	_, err = tx.Exec("DELETE FROM sentences")
	if err != nil {
		tx.Rollback()
		return err
	}

	return tx.Commit()
}
