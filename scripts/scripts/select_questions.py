#!/usr/bin/env python

import sys
import argparse
import json
from typing import List, Dict, Any, TextIO
import uuid


def load_entries(filepath: str) -> List[Dict[str, Any]]:
    if filepath == "-":
        data = json.load(sys.stdin)
    else:
        with open(filepath, "r", encoding="utf-8") as f:
            data = json.load(f)
    return data


def get_output_file(output_path: str) -> TextIO:
    if output_path == "-":
        return sys.stdout
    return open(output_path, "w", encoding="utf-8")


def process_entries(entries: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    selected_questions: List[Dict[str, Any]] = []
    for entry in entries:
        questions = entry["questions"]
        index = int(entry["idx"])
        sentence_id = entry["id"]
        language = entry["sourceLanguage"]
        sourceWordExplanations = entry["sourceWordExplanations"]
        questions = [
            dict(
                question,
                id=str(uuid.uuid4()),
                index=index,
                sentence_id=sentence_id,
                language=language,
                difficulty=(index + 1) * 10,
                sourceWordExplanations=sourceWordExplanations,
            )
            for question in questions
        ]
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
    return parser


def main(filepath: str, output: str) -> None:
    entries = load_entries(filepath)
    selected = process_entries(entries)

    output_file = get_output_file(output)
    needs_closing = output != "-"
    try:
        json.dump(selected, output_file, ensure_ascii=False, indent=2)
    finally:
        if needs_closing:
            output_file.close()


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.filepath, args.output)
