#!/usr/bin/env python

from typing import Dict, List, Any
import json
import sys
import argparse
import psycopg2
from psycopg2.extras import register_uuid, Json

register_uuid()


def load_questions(filepath: str) -> List[Dict[str, Any]]:
    """
    Load questions from a JSON file or stdin.
    Args:
        filepath: Path to the JSON file containing questions, or '-' to read from stdin
    Returns:
        A list of dictionaries with question data
    """
    try:
        if filepath == "-":
            return json.load(sys.stdin)
        else:
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


def update_questions(conn, questions: List[Dict[str, Any]]):
    """
    Batch update question_type_specific_data for questions using executemany.
    Only processes entries with both 'id' and 'question_type_specific_data'.
    """
    try:
        with conn.cursor() as cur:
            valid = [
                (Json(q["question_type_specific_data"]), q["id"])
                for q in questions
                if q.get("id")
                and q.get("question_type_specific_data") is not None
            ]
            if not valid:
                print("No valid questions to update.", file=sys.stderr)
                return
            sql = "UPDATE questions SET question_type_specific_data = %s WHERE id = %s"
            cur.executemany(sql, valid)
            conn.commit()
            print(
                f"Successfully updated {len(valid)} questions' question_type_specific_data in the database."
            )
    except Exception as e:
        conn.rollback()
        print(f"Error updating questions in the database: {e}", file=sys.stderr)
        sys.exit(1)


def get_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Update questions in the PostgreSQL database from a JSON file by ID"
    )
    parser.add_argument(
        "filepath",
        nargs="?",
        default="-",
        help="Path to the JSON file containing questions to update (default: '-' to read from stdin)",
    )
    parser.add_argument(
        "-d",
        "--db-url",
        default="postgresql://postgres:postgres@localhost/lingetic",
        help="Database connection URL (default: postgresql://postgres:postgres@localhost/lingetic)",
    )
    return parser


def main(filepath: str, db_url: str):
    questions = load_questions(filepath)
    conn = connect_to_database(db_url)
    try:
        update_questions(conn, questions)
    finally:
        conn.close()


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.filepath, args.db_url)
