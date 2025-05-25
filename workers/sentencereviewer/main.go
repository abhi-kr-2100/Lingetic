package main

import (
	"database/sql"
	"log"
	"net/http"
	"os"

	_ "github.com/lib/pq"

	"munetmo.com/lingetic/workers/sentencereviewer/db"
	"munetmo.com/lingetic/workers/sentencereviewer/server"
)

func main() {
	secret := os.Getenv("CLOUDAMQP_WEBHOOK_SECRET_KEY")
	if secret == "" {
		log.Fatalf("CLOUDAMQP_WEBHOOK_SECRET_KEY env var not set")
	}

	dbConn, err := sql.Open("postgres", db.BuildDBConnURL())
	if err != nil {
		log.Fatalf("Unable to open database: %v", err)
	}
	defer dbConn.Close()

	if err := dbConn.Ping(); err != nil {
		log.Fatalf("Unable to connect to database: %v", err)
	}

	sentenceRepo := &db.SentenceRepository{DB: dbConn}
	reviewRepo := &db.ReviewRepository{DB: dbConn, SentenceRepo: sentenceRepo}

	server := &server.Server{
		SecretKey:    secret,
		SentenceRepo: sentenceRepo,
		ReviewRepo:   reviewRepo,
	}

	addr := ":8080"
	log.Printf("Listening on %s\n", addr)
	log.Fatal(http.ListenAndServe(addr, server))
}
