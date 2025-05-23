#!/usr/bin/env python

from typing import Dict, List, Any, Optional
import json
import sys
import argparse
import uuid
import psycopg2
from psycopg2.extras import register_uuid, execute_values

register_uuid()


def load_questions(filepath: str) -> List[Dict[str, Any]]:
    """
    Load questions from a JSON file or stdin.

    Args:
        filepath: Path to the JSON file containing questions, or '-' to read from stdin

    Returns:
        A list of dictionaries with question_type and question_type_specific_data
    """
    if filepath == "-":
        return json.load(sys.stdin)
    with open(filepath, "r", encoding="utf-8") as file:
        return json.load(file)


def connect_to_database(db_url: str):
    """
    Connect to the PostgreSQL database using the provided connection URL.

    Args:
        db_url: Database connection URL

    Returns:
        A database connection object
    """
    conn = psycopg2.connect(db_url)
    return conn


def insert_question_list(conn, name: str, language: str) -> uuid.UUID:
    """
    Insert a new question list into the database.

    Args:
        conn: Database connection object
        name: Name of the question list
        language: Language of the questions

    Returns:
        UUID of the created question list
    """
    with conn.cursor() as cur:
        question_list_id = uuid.uuid4()
        cur.execute(
            """
            INSERT INTO question_lists (id, name, language)
            VALUES (%s, %s, %s)
            RETURNING id
            """,
            (question_list_id, name, language),
        )
        conn.commit()
        return question_list_id


def insert_questions(
    conn,
    questions: List[Dict[str, Any]],
    question_list_id: uuid.UUID,
):
    """
    Insert questions into the database using batch insertion.

    Args:
        conn: Database connection object
        questions: List of dictionaries containing question_type, and question_type_specific_data
        question_list_id: UUID for the question list
    """
    try:
        with conn.cursor() as cur:
            values = [
                (
                    question["id"],
                    question["question_type"],
                    question["language"],
                    question["difficulty"],
                    question_list_id,
                    json.dumps(question["question_type_specific_data"]),
                    question["sentence_id"],
                )
                for question in questions
            ]

            execute_values(
                cur,
                """
                INSERT INTO questions (
                    id, question_type, language, difficulty,
                    question_list_id, question_type_specific_data, sentence_id
                ) VALUES %s
                """,
                values,
                template="(%s, %s, %s, %s, %s, %s, %s)",
            )

            conn.commit()
    except Exception:
        conn.rollback()
        raise


def main(
    filepath: str,
    db_url: str,
    language: str,
    question_list_id: Optional[str] = None,
    question_list_name: Optional[str] = None,
):
    """
    Main function to process questions and insert them into the database.

    Args:
        filepath: Path to the JSON file containing questions, or '-' to read from stdin
        db_url: Database connection URL
        language: Language of the questions
        question_list_id: Optional UUID string for the question list
        question_list_name: Optional name for creating a new question list
    """
    questions = load_questions(filepath)
    conn = connect_to_database(db_url)

    try:
        uuid_obj = None
        if question_list_id:
            uuid_obj = uuid.UUID(question_list_id)
        elif question_list_name:
            uuid_obj = insert_question_list(conn, question_list_name, language)
        else:
            raise ValueError(
                "Error: Either --question-list-id or --question-list-name must be provided"
            )

        insert_questions(conn, questions, uuid_obj)
    finally:
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
    parser.add_argument(
        "-n",
        "--question-list-name",
        help="Name for creating a new question list (ignored if --question-list-id is provided)",
    )
    return parser


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()

    main(
        args.filepath,
        args.db_url,
        args.language,
        args.question_list_id,
        args.question_list_name,
    )
