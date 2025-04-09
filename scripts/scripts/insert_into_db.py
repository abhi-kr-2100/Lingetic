#!/usr/bin/env python

from typing import Dict, List, Any, Optional
import json
import sys
import argparse
import uuid
import psycopg2
import psycopg2.extras  # Import extras module which contains the UUID adapter


# Register UUID adapter with psycopg2
psycopg2.extras.register_uuid()


def load_questions(filepath: str) -> List[Dict[str, Any]]:
    """
    Load questions from a JSON file or stdin.

    Args:
        filepath: Path to the JSON file containing questions, or '-' to read from stdin

    Returns:
        A list of dictionaries with question_type and question_type_specific_data
    """
    try:
        if filepath == "-":
            # Read from stdin
            return json.load(sys.stdin)
        else:
            # Read from file
            with open(filepath, "r", encoding="utf-8") as file:
                return json.load(file)
    except FileNotFoundError:
        print(f"Error: File '{filepath}' not found", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError:
        source = "stdin" if filepath == "-" else f"file '{filepath}'"
        print(f"Error: Invalid JSON format in {source}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"Error loading questions: {e}", file=sys.stderr)
        sys.exit(1)


def connect_to_database(db_url: str):
    """
    Connect to the PostgreSQL database using the provided connection URL.

    Args:
        db_url: Database connection URL

    Returns:
        A database connection object
    """
    try:
        conn = psycopg2.connect(db_url)
        return conn
    except Exception as e:
        print(f"Error connecting to the database: {e}", file=sys.stderr)
        sys.exit(1)


def insert_questions(
    conn,
    questions: List[Dict[str, Any]],
    language: str,
    question_list_id: Optional[uuid.UUID] = None,
):
    """
    Insert questions into the database.

    Args:
        conn: Database connection object
        questions: List of dictionaries containing question_type, and question_type_specific_data
        language: Language of the questions
        question_list_id: Optional UUID for the question list, generated if None
    """
    try:
        # Create a cursor object that can handle UUIDs
        with conn.cursor() as cur:
            # Use provided question_list_id or generate a new one
            if question_list_id is None:
                question_list_id = uuid.uuid4()

            # Insert each question into the database
            for index, question in enumerate(questions):
                # Generate a unique id for each question
                question_id = uuid.uuid4()

                # Set difficulty based on question index (starting from 10, incrementing by 10)
                difficulty = (index + 1) * 10

                # Create the question_type_specific_data as a JSON
                question_type = question.get("question_type")
                question_data = question.get("question_type_specific_data", {})

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
                        question_type,
                        language,
                        difficulty,
                        question_list_id,
                        json.dumps(question_data),
                    ),
                )

            # Commit the transaction
            conn.commit()
            print(
                f"Successfully inserted {len(questions)} questions into the database."
            )
            print(f"Used question_list_id: {question_list_id}")

    except Exception as e:
        conn.rollback()
        print(
            f"Error inserting questions into the database: {e}", file=sys.stderr
        )
        sys.exit(1)


def main(
    filepath: str,
    db_url: str,
    language: str,
    question_list_id: Optional[str] = None,
):
    """
    Main function to process questions and insert them into the database.

    Args:
        filepath: Path to the JSON file containing questions, or '-' to read from stdin
        db_url: Database connection URL
        language: Language of the questions
        question_list_id: Optional UUID string for the question list
    """
    # Load questions from the JSON file
    questions = load_questions(filepath)

    # Connect to the database
    conn = connect_to_database(db_url)

    # Convert question_list_id string to UUID if provided
    uuid_obj = None
    if question_list_id:
        try:
            uuid_obj = uuid.UUID(question_list_id)
        except ValueError:
            print(
                f"Error: Invalid UUID format: '{question_list_id}'",
                file=sys.stderr,
            )
            sys.exit(1)

    try:
        # Insert questions into the database
        insert_questions(conn, questions, language, uuid_obj)
    finally:
        # Close the database connection
        conn.close()


def get_parser() -> argparse.ArgumentParser:
    """
    Create and configure the argument parser for the script.

    Returns:
        An ArgumentParser object configured with the script's arguments
    """
    parser = argparse.ArgumentParser(
        description="Insert fill-in-the-blank questions from a JSON file into the PostgreSQL database"
    )
    parser.add_argument(
        "filepath",
        nargs="?",
        default="-",
        help="Path to the JSON file containing questions (default: '-' to read from stdin)",
    )
    parser.add_argument(
        "-d",
        "--db-url",
        default="postgresql://postgres:postgres@localhost/lingetic",
        help="Database connection URL (default: postgresql://postgres:postgres@localhost/lingetic)",
    )
    parser.add_argument(
        "-l",
        "--language",
        required=True,
        help="Language of the questions (e.g., 'Turkish', 'Spanish', 'French')",
    )
    parser.add_argument(
        "-q",
        "--question-list-id",
        help="UUID for the question list (default: randomly generated)",
    )
    return parser


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()

    main(args.filepath, args.db_url, args.language, args.question_list_id)
