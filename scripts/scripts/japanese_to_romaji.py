#!/usr/bin/env python3
"""
Convert Japanese text to Romaji using the cutlet library.

This script reads a JSON file containing Japanese sentences and converts them to Romaji
using the Hepburn romanization system, while preserving all other fields in the input.
"""

import argparse
import json
from typing import Dict, Any, List

import cutlet

katsu = cutlet.Cutlet(use_foreign_spelling=False)


def convert_to_romaji(text: str) -> str:
    """Convert Japanese text to Romaji using the cutlet library.

    Args:
        text: Japanese text to convert to Romaji

    Returns:
        str: The converted Romaji text
    """
    return katsu.romaji(text)


def get_parser() -> argparse.ArgumentParser:
    """Configure command line argument parser.

    Returns:
        argparse.ArgumentParser: Configured argument parser
    """
    parser = argparse.ArgumentParser(
        description="Convert Japanese text to Romaji in a JSON file."
    )
    parser.add_argument(
        "input_file", type=str, help="Path to the input JSON file"
    )
    parser.add_argument(
        "-o",
        "--output",
        type=str,
        default="-",
        help="Output file path (default: stdout)",
    )
    return parser


def main(input_file: str, output: str):
    """Main function to handle command line arguments and process the input file."""
    with open(input_file, "r", encoding="utf-8") as f:
        data = json.load(f)

    def process_sentence(sentence: Dict[str, Any]) -> Dict[str, Any]:
        processed = sentence.copy()
        processed["sourceText"] = convert_to_romaji(processed["sourceText"])
        return processed

    processed_sentences: List[Dict[str, Any]] = [
        process_sentence(sentence) for sentence in data["sentences"]
    ]

    result = {"sentences": processed_sentences}
    output_to_write = json.dumps(result, ensure_ascii=False, indent=2)

    if output == "-":
        print(output_to_write)
    else:
        with open(output, "w", encoding="utf-8") as f:
            f.write(output_to_write)


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(
        input_file=args.input_file,
        output=args.output,
    )
