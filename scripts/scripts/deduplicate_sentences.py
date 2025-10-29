import argparse
import json
import sys
from pathlib import Path
from typing import List, Dict, Any, Set


def get_parser() -> argparse.ArgumentParser:
    """Configure command line argument parser."""
    parser = argparse.ArgumentParser(
        description=(
            "Deduplicate sentences in a single JSON object by the 'sourceText' field."
        )
    )
    parser.add_argument(
        "input",
        nargs="?",
        default="-",
        help=(
            "Path to the input file. Defaults to stdin."
        ),
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
    """Main function to deduplicate sentence JSON objects by 'sourceText'."""
    content = get_content(input)
    obj: Dict[str, Any] = json.loads(content)
    all_sentences: List[Dict[str, Any]] = obj["sentences"]

    seen: Set[str] = set()
    deduped: List[Dict[str, Any]] = []

    for s in all_sentences:
        key = s["sourceText"]

        if key in seen:
            continue

        seen.add(key)
        deduped.append(s)

    final_object = {"sentences": deduped}

    with get_output_file(output) as f:
        json.dump(final_object, f, ensure_ascii=False, indent=2)
