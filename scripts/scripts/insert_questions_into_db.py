#!/usr/bin/env python

from typing import Dict, List, Any
import json
import sys
import argparse
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


def insert_questions(
    conn,
    questions: List[Dict[str, Any]],
):
    """
    Insert questions into the database using batch insertion.

    Args:
        conn: Database connection object
        questions: List of dictionaries containing question_type, and question_type_specific_data
    """
    try:
        with conn.cursor() as cur:
            values = [
                (
                    question["id"],
                    question["question_type"],
                    question["language"],
                    json.dumps(question["question_type_specific_data"]),
                    question["sentence_id"],
                    json.dumps(question["sourceWordExplanations"]),
                )
                for question in questions
            ]

            execute_values(
                cur,
                """
                INSERT INTO questions (
                    id, question_type, language,
                    question_type_specific_data, sentence_id,
                    source_word_explanations
                ) VALUES %s
                """,
                values,
                template="(%s, %s, %s, %s, %s, %s)",
            )

            conn.commit()
    except Exception:
        conn.rollback()
        raise


def main(filepath: str, db_url: str):
    """
    Main function to process questions and insert them into the database.

    Args:
        filepath: Path to the JSON file containing questions, or '-' to read from stdin
        db_url: Database connection URL
    """
    questions = load_questions(filepath)
    conn = connect_to_database(db_url)

    try:
        insert_questions(conn, questions)
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
    return parser


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()

    main(
        args.filepath,
        args.db_url,
    )
