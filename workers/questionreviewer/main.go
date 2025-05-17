package main

import (
	"database/sql"
	"log"
	"net/http"
	"os"

	_ "github.com/lib/pq"

	"munetmo.com/lingetic/workers/questionreviewer/db"
	"munetmo.com/lingetic/workers/questionreviewer/server"
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
	log.Println("Connected to database.")

	qrepo := &db.QuestionRepository{DB: dbConn}
	rrepo := &db.ReviewRepository{DB: dbConn, QuestionRepo: qrepo}

	srv := &server.Server{
		SecretKey:    secret,
		QuestionRepo: qrepo,
		ReviewRepo:   rrepo,
	}

	addr := ":8080"
	log.Printf("QuestionReviewer worker listening at %s (POST /?key=...)", addr)
	log.Fatal(http.ListenAndServe(addr, srv))
}
