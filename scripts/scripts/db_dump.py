#!/usr/bin/env python

from typing import Dict, List, Any
import json
import sys
import argparse
import psycopg2
from psycopg2.extras import register_uuid
from psycopg2 import sql

register_uuid()


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


def dump_table(conn, table: str, language: str) -> List[Dict[str, Any]]:
    """
    Dump all rows from the specified table filtered by language.

    Args:
        conn: Database connection object
        table: Name of the table to dump
        language: Language filter for rows

    Returns:
        A list of dictionaries representing table rows
    """
    try:
        with conn.cursor() as cur:
            query = sql.SQL("SELECT * FROM {table} WHERE language = %s").format(
                table=sql.Identifier(table)
            )
            cur.execute(query, (language,))
            rows = cur.fetchall()
            columns = [desc[0] for desc in cur.description]
            data = [dict(zip(columns, row)) for row in rows]
            return data
    except Exception as e:
        print(f"Error dumping table '{table}': {e}", file=sys.stderr)
        sys.exit(1)


def write_output(data: List[Dict[str, Any]], output: str):
    """
    Write data to JSON file or stdout.

    Args:
        data: List of dictionaries representing rows
        output: Output file path or '-' for stdout
    """
    try:
        if output == "-":
            json.dump(
                data, sys.stdout, ensure_ascii=False, indent=2, default=str
            )
            print()
        else:
            with open(output, "w", encoding="utf-8") as f:
                json.dump(data, f, ensure_ascii=False, indent=2, default=str)
    except Exception as e:
        print(f"Error writing output: {e}", file=sys.stderr)
        sys.exit(1)


def main(db_url: str, language: str, output: str):
    """
    Main function to dump data from a table.

    Args:
        db_url: Database connection URL
        language: Language filter for rows
        output: Output file path or '-' for stdout
    """
    table = "questions"
    conn = connect_to_database(db_url)
    try:
        data = dump_table(conn, table, language)
        write_output(data, output)
    finally:
        conn.close()


def get_parser() -> argparse.ArgumentParser:
    """
    Create and configure the argument parser for the script.

    Returns:
        An ArgumentParser object configured with the script's arguments
    """
    parser = argparse.ArgumentParser(
        description="Dump data from the 'questions' table to JSON"
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
        help="Language filter for rows (e.g., 'Turkish', 'Spanish', 'French')",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="-",
        help="Output JSON file path (default: '-' to write to stdout)",
    )
    return parser


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()

    main(args.db_url, args.language, args.output)
