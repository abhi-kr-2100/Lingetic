#!/usr/bin/env python3

import json
import sys
import argparse
from typing import Dict, List, Any
import psycopg2
from psycopg2.extras import register_uuid, execute_values


register_uuid()


def load_sentences(filepath: str) -> List[Dict[str, Any]]:
    """
    Load sentences from a JSON file or stdin.

    Args:
        filepath: Path to the JSON file containing sentences, or '-' to read from stdin

    Returns:
        A list of sentence dictionaries
    """
    if filepath == "-":
        data = json.load(sys.stdin)
    else:
        with open(filepath, "r", encoding="utf-8") as file:
            data = json.load(file)

    return data.get("sentences", [])


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


def insert_sentences(conn, sentences: List[Dict[str, Any]]):
    """
    Insert sentences into the database using batch insertion.

    Args:
        conn: Database connection object
        sentences: List of sentence dictionaries to insert
    """
    try:
        with conn.cursor() as cur:
            values = [
                (
                    sentence["id"],
                    sentence["sourceLanguage"],
                    sentence["sourceText"],
                    sentence["translationLanguage"],
                    sentence["translationText"],
                    json.dumps(sentence.get("sourceWordExplanation", [])),
                )
                for sentence in sentences
            ]

            execute_values(
                cur,
                """
                INSERT INTO sentences (
                    id, source_language, source_text,
                    translation_language, translation_text, source_word_explanations
                ) VALUES %s
                """,
                values,
                template="(%s, %s, %s, %s, %s, %s)",
            )

            conn.commit()
    except Exception:
        conn.rollback()
        raise


def main(file: str, db_url: str):
    """
    Main function to process sentences and insert them into the database.

    Args:
        file: Path to the JSON file containing sentences, or '-' to read from stdin
        db_url: Database connection URL
    """
    sentences = load_sentences(file)
    conn = connect_to_database(db_url)

    try:
        insert_sentences(conn, sentences)
    finally:
        conn.close()


def get_parser():
    """
    Create and configure the argument parser for the script.

    Returns:
        An ArgumentParser object configured with the script's arguments
    """
    parser = argparse.ArgumentParser(
        description="Insert sentences into the database from a JSON file."
    )
    parser.add_argument(
        "file",
        nargs="?",
        default="-",
        help="Path to the JSON file containing sentences (default: read from stdin)",
    )
    parser.add_argument(
        "-d",
        "--db-url",
        default="postgresql://postgres:postgres@localhost:5432/lingetic",
        help="Database connection URL",
    )
    return parser


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()

    main(args.file, args.db_url)
