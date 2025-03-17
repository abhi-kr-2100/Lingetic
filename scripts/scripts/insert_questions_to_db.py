#!/usr/bin/env python

from typing import Dict, List, Any
import json
import sys
import random
import argparse
import uuid
import psycopg2
import psycopg2.extras  # Import extras module which contains the UUID adapter


# Register UUID adapter with psycopg2
psycopg2.extras.register_uuid()


def load_questions(filepath: str) -> List[Dict[str, Any]]:
    """
    Load questions from a JSON file.

    Args:
        filepath: Path to the JSON file containing questions

    Returns:
        A list of dictionaries with questionText, answer, and hint fields
    """
    try:
        with open(filepath, "r", encoding="utf-8") as file:
            return json.load(file)
    except FileNotFoundError:
        print(f"Error: File '{filepath}' not found", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError:
        print(
            f"Error: Invalid JSON format in file '{filepath}'", file=sys.stderr
        )
        sys.exit(1)
    except Exception as e:
        print(f"Error loading questions: {e}", file=sys.stderr)
        sys.exit(1)


def connect_to_database():
    """
    Connect to the PostgreSQL database.

    Returns:
        A database connection object
    """
    try:
        # Connect to the PostgreSQL database
        conn = psycopg2.connect(
            host="localhost",
            dbname="lingetic",
            user="postgres",
            password="postgres",
        )
        return conn
    except Exception as e:
        print(f"Error connecting to the database: {e}", file=sys.stderr)
        sys.exit(1)


def insert_questions(conn, questions: List[Dict[str, Any]]):
    """
    Insert questions into the database.

    Args:
        conn: Database connection object
        questions: List of dictionaries containing questionText, answer, and hint
    """
    try:
        # Create a cursor object that can handle UUIDs
        with conn.cursor() as cur:
            # Generate a single question_list_id for all questions in this batch
            question_list_id = uuid.uuid4()

            # Insert each question into the database
            for question in questions:
                # Generate a unique id for each question
                question_id = uuid.uuid4()

                # Generate a random difficulty level between 1 and 5
                difficulty = random.randint(1, 5)

                # Create the question_type_specific_data as a JSON
                question_data = {
                    "questionText": question.get("questionText", ""),
                    "answer": question.get("answer", ""),
                    "hint": question.get("hint", ""),
                }

                # Insert the question into the database
                cur.execute(
                    """
                    INSERT INTO questions (
                        id, question_type, language, difficulty, 
                        question_list_id, question_type_specific_data
                    ) VALUES (%s, %s, %s, %s, %s, %s)
                    """,
                    (
                        question_id,
                        "FillInTheBlanks",
                        "Turkish",
                        difficulty,
                        question_list_id,
                        json.dumps(question_data),
                    ),
                )

            # Commit the transaction
            conn.commit()
            print(
                f"Successfully inserted {len(questions)} questions into the database.",
                file=sys.stderr,
            )
            print(f"Used question_list_id: {question_list_id}", file=sys.stderr)

    except Exception as e:
        conn.rollback()
        print(
            f"Error inserting questions into the database: {e}", file=sys.stderr
        )
        sys.exit(1)


def main(filepath: str):
    """
    Main function to process questions and insert them into the database.

    Args:
        filepath: Path to the JSON file containing questions
    """
    # Load questions from the JSON file
    questions = load_questions(filepath)

    # Connect to the database
    conn = connect_to_database()

    try:
        # Insert questions into the database
        insert_questions(conn, questions)
    finally:
        # Close the database connection
        conn.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Insert fill-in-the-blank questions from a JSON file into the PostgreSQL database"
    )
    parser.add_argument(
        "filepath", help="Path to the JSON file containing questions"
    )
    args = parser.parse_args()

    main(args.filepath)
