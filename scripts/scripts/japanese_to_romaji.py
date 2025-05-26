#!/usr/bin/env python3
"""
Convert Japanese text to Romaji using the Gemini API.

This script reads a JSON file containing Japanese sentences and converts them to Romaji
using the Hepburn romanization system, while preserving all other fields in the input.
"""

import argparse
import json
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Dict, List, Any, Tuple
from pydantic import BaseModel

from library.gemini_client import get_global_gemini_client

SYSTEM_PROMPT = """Convert the following Japanese text to Romaji using the Hepburn romanization system.

Rules:
1. Use only regular ASCII characters (no special Unicode characters like macrons or apostrophes)
2. Use standard Hepburn romanization
3. Keep the original punctuation and spacing
4. Do not translate the text, only convert it to Romaji
5. Output just the converted text, nothing else
6. Capitalize correctly.

Example Input: こんにちは
Example Output: Konnichiwa

Input: """


class RomajiResponse(BaseModel):
    """Pydantic model for the Romaji conversion response."""

    romaji: str


def convert_to_romaji(text: str) -> str:
    """Convert Japanese text to Romaji using the Gemini API.

    Args:
        text: Japanese text to convert to Romaji

    Returns:
        str: The converted Romaji text
    """
    client = get_global_gemini_client()
    prompt = SYSTEM_PROMPT
    prompt += f"{text}\n"

    response = client.generate_content(
        prompt=prompt,
        response_schema=RomajiResponse,
    )

    return response["romaji"]


def process_single_sentence(
    idx: int,
    sentence: Dict[str, Any],
    cache_filepath: Path,
    log_lock: threading.Lock,
) -> Tuple[int, Dict[str, Any]]:
    """Process a single sentence and update the cache.

    Args:
        idx: Index of the sentence in the original list
        sentence: The sentence dictionary to process
        cache_filepath: Path to the cache file
        log_lock: Thread lock for logging

    Returns:
        Tuple of (index, processed_sentence) if successful
    """
    processed = sentence.copy()
    processed["sourceText"] = convert_to_romaji(processed["sourceText"])
    processed["original_index"] = idx

    with log_lock:
        with open(cache_filepath, "a", encoding="utf-8") as f:
            json.dump(processed, f, ensure_ascii=False)
            f.write("\n")

    return idx, processed


def process_sentences_parallel(
    sentences: List[Dict[str, Any]], cache_file_path: Path
) -> List[Dict[str, Any]]:
    """Process sentences in parallel using ThreadPoolExecutor.

    Args:
        sentences: List of sentence dictionaries to process
        cache_file_path: Path to the cache file

    Returns:
        List of processed sentences in original order
    """
    cache_file_path.parent.mkdir(parents=True, exist_ok=True)
    cache_file_path.touch(exist_ok=True)

    log_lock = threading.Lock()

    results = {}
    with ThreadPoolExecutor() as executor:
        futures = [
            executor.submit(
                process_single_sentence,
                idx,
                sentence,
                cache_file_path,
                log_lock,
            )
            for idx, sentence in enumerate(sentences)
        ]

        for future in as_completed(futures):
            result = future.result()
            idx, processed = result
            results[idx] = processed

    return [results[i] for i in sorted(results.keys())]


def load_processed_sentences(
    cache_file_path: Path,
) -> Dict[int, Dict[str, Any]]:
    """Load already processed sentences from cache file.

    Args:
        cache_file_path: Path to the cache file

    Returns:
        Dictionary mapping original indices to processed sentences
    """
    if not cache_file_path.exists():
        return {}

    processed = {}
    with open(cache_file_path, "r", encoding="utf-8") as f:
        for line in f:
            if line.strip():
                sentence = json.loads(line)
                idx = sentence["original_index"]
                processed[idx] = sentence

    return processed


def get_cache_file_path(output: str) -> Path:
    """Determine the cache file path based on the output path.

    Args:
        output: Output file path or '-' for stdout

    Returns:
        Path to the cache file
    """
    if output == "-":
        output_path = Path("romaji_cache.jsonl")
    else:
        output_path = Path(output)
        cache_dir = output_path.parent / "cache"
        cache_dir.mkdir(exist_ok=True)
        output_path = cache_dir / f"{output_path.stem}_romaji_cache.jsonl"

    return output_path


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

    cache_file_path = get_cache_file_path(output)
    cached_results = load_processed_sentences(cache_file_path)
    cached_indices = set(cached_results.keys())

    to_process = [
        (i, s)
        for i, s in enumerate(data["sentences"])
        if i not in cached_indices
    ]

    processed_new = process_sentences_parallel(
        [s for _, s in to_process], cache_file_path
    )

    for (idx, _), processed in zip(to_process, processed_new):
        processed["original_index"] = idx
        cached_results[idx] = processed

    processed_sentences = [
        cached_results[i]
        for i in range(len(data["sentences"]))
        if i in cached_results
    ]

    result = {"sentences": processed_sentences}
    output_to_write = json.dumps(result, ensure_ascii=False, indent=2)

    if output == "-":
        print(output_to_write)
    else:
        output_path = Path(output)
        output_path.parent.mkdir(parents=True, exist_ok=True)
        with open(output_path, "w", encoding="utf-8") as f:
            f.write(output_to_write)


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(
        input_file=args.input_file,
        output=args.output,
    )
