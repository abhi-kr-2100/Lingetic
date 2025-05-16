#!/usr/bin/env python

import sys
import argparse
import json
from typing import List, Dict, Any, TextIO


def load_entries(filepath: str) -> List[Dict[str, Any]]:
    try:
        if filepath == "-":
            data = json.load(sys.stdin)
        else:
            with open(filepath, "r", encoding="utf-8") as f:
                data = json.load(f)
        return data
    except Exception as e:
        print(f"Error loading entries: {e}", file=sys.stderr)
        sys.exit(1)


def get_output_file(output_path: str) -> TextIO:
    if output_path == "-":
        return sys.stdout
    try:
        return open(output_path, "w", encoding="utf-8")
    except Exception as e:
        print(
            f"Error opening output file '{output_path}': {e}", file=sys.stderr
        )
        sys.exit(1)


def process_entries(
    entries: List[Dict[str, Any]], language: str
) -> List[Dict[str, Any]]:
    selected_questions: List[Dict[str, Any]] = []
    for entry in entries:
        questions = entry.get("questions", [])
        index = entry.get("index")
        if index is not None:
            questions = [dict(question, index=index) for question in questions]
        if not questions:
            continue
        # Add all questions from this entry
        selected_questions.extend(questions)
    return selected_questions


def get_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Select fill-in-the-blank questions with largest answers."
    )
    parser.add_argument(
        "filepath",
        nargs="?",
        default="-",
        help="Path to the JSON file containing entries (default: '-' to read from stdin)",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="-",
        help="Path to the output file (default: '-' for stdout)",
    )
    parser.add_argument(
        "-l",
        "--language",
        required=True,
        help="Language (case-sensitive!)",
    )
    return parser


def main(filepath: str, output: str, language: str) -> None:
    entries = load_entries(filepath)
    selected = process_entries(entries, language)
    output_file = get_output_file(output)
    needs_closing = output != "-"
    try:
        json.dump(selected, output_file, ensure_ascii=False, indent=2)
        output_file.write("\n")
    except Exception as e:
        print(f"Error writing output: {e}", file=sys.stderr)
        sys.exit(1)
    finally:
        if needs_closing:
            output_file.close()


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.filepath, args.output, args.language)
