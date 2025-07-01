import argparse
import json
import sys
from pathlib import Path
from typing import List, Dict, Any


def get_parser() -> argparse.ArgumentParser:
    """Configure command line argument parser."""
    parser = argparse.ArgumentParser(
        description="Combine multiple sentence JSON objects from a file into one."
    )
    parser.add_argument(
        "input",
        nargs="?",
        default="-",
        help="Path to the input file containing one or more JSON objects. Defaults to stdin.",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="-",
        help="Path to the output file. Defaults to stdout.",
    )
    return parser


def get_output_file(output_path: str) -> Any:
    """Get file handle for output (stdout or file)"""
    if output_path == "-":
        return sys.stdout
    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    return open(output_path, "w", encoding="utf-8")


def get_content(input_path: str) -> str:
    """Reads content from a file or stdin."""
    if input_path == "-":
        return sys.stdin.read().strip()
    with open(input_path, "r", encoding="utf-8") as f:
        return f.read().strip()


def main(input: str, output: str):
    """Main function to combine sentence JSON objects."""
    content = get_content(input)

    decoder = json.JSONDecoder()
    objects: List[Dict[str, Any]] = []
    pos = 0
    while pos < len(content):
        obj, end_pos = decoder.raw_decode(content[pos:])
        objects.append(obj)
        pos += end_pos
        # Skip whitespace between objects
        while pos < len(content) and content[pos].isspace():
            pos += 1

    combined_sentences = []
    for obj in objects:
        combined_sentences.extend(obj["sentences"])

    final_object = {"sentences": combined_sentences}

    with get_output_file(output) as f:
        json.dump(final_object, f, ensure_ascii=False, indent=2)


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.input, args.output)
