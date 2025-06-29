#!/usr/bin/env python3
"""
Convert Japanese text to Romaji using the Gemini API.

This script reads a JSON file containing Japanese sentences and converts them to Romaji
using the Hepburn romanization system, while preserving all other fields in the input.
"""

import argparse
import json
import uuid
from pydantic import BaseModel

from library.gemini_client import get_global_gemini_client

SYSTEM_PROMPT = """Convert the following Japanese text to Romaji using the Hepburn romanization system.

Rules:
1. Use standard Hepburn romanization.
2. Keep the original punctuation and spacing.
3. Do not translate the text, only convert it to Romaji.
4. Output just the converted text, nothing else.
5. Capitalize correctly.
6. Use macrons and apostrophes where appropriate.

Example Input: こんにちは
Example Output: Konnichiwa

Input: """


class RomajiResponse(BaseModel):
    """Pydantic model for the Romaji conversion response."""

    romaji: str


def convert_to_romaji(
    text: str,
) -> str:
    """Convert Japanese text to Romaji using the Gemini API.

    Args:
        text: Japanese text to convert to Romaji

    Returns:
        str: The converted Romaji text
    """
    client = get_global_gemini_client()

    prompt = SYSTEM_PROMPT + f"{text}\n"

    request_id = uuid.uuid5(uuid.NAMESPACE_DNS, f'romaji-{text}')
    response = client.generate_content(
        prompt=prompt,
        response_schema=RomajiResponse,
        request_id=request_id
    )

    return response["romaji"]


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

    processed_sentences = []
    for sentence in data["sentences"]:
        processed = sentence.copy()
        processed["sourceText"] = convert_to_romaji(processed["sourceText"])
        processed_sentences.append(processed)

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
