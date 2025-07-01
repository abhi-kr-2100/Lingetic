import argparse
import json
import sys
import uuid
import logging
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import List, Dict, Any

from pydantic import BaseModel

from library.gemini_client import get_global_gemini_client

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler(sys.stderr)],
)
logger = logging.getLogger(__name__)


class DifficultyRating(BaseModel):
    difficulty: int


def get_parser() -> argparse.ArgumentParser:
    """Configure command line argument parser."""
    parser = argparse.ArgumentParser(
        description="Enrich sentence data with IDs, languages, and difficulty ratings."
    )
    parser.add_argument(
        "input",
        nargs="?",
        default="-",
        help="Path to the input file. Defaults to stdin.",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="-",
        help="Path to the output file. Defaults to stdout.",
    )
    parser.add_argument(
        "-s",
        "--source-language",
        required=True,
        help="Source language of the sentences.",
    )
    parser.add_argument(
        "-t",
        "--translation-language",
        default="English",
        help="Translation language of the sentences.",
    )
    return parser


def get_content(input_path: str) -> str:
    """Reads content from a file or stdin."""
    if input_path == "-":
        return sys.stdin.read().strip()
    with open(input_path, "r", encoding="utf-8") as f:
        return f.read().strip()


def get_output_file(output_path: str) -> Any:
    """Get file handle for output (stdout or file)"""
    if output_path == "-":
        return sys.stdout
    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    return open(output_path, "w", encoding="utf-8")


def get_prompt_template() -> str:
    """Reads the prompt template from the file."""
    with open("../prompts/rate_sentence_difficulty.md", "r", encoding="utf-8") as f:
        return f.read()


def get_difficulty_from_llm(sentence: str, prompt_template: str) -> int:
    """Gets the difficulty of a sentence from the LLM."""
    client = get_global_gemini_client()
    prompt = prompt_template + f"\n\nSentence: \"{sentence}\""
    response = client.generate_content(prompt, response_schema=DifficultyRating)
    return response["difficulty"]


def get_cache_file_path(output: str) -> Path:
    """Determine the cache file path based on the output path."""
    return (
        Path(output).with_suffix(".cache")
        if output != "-"
        else Path("output.cache")
    )


def load_processed_entries(
    cache_file_path: Path,
) -> tuple[set[int], dict[int, Dict[str, Any]]]:
    """Load already processed entries from cache file."""
    processed_idxes = set()
    cached_results = {}

    if not cache_file_path.exists():
        return processed_idxes, cached_results

    with open(cache_file_path, "r", encoding="utf-8") as cache_file:
        for line in cache_file:
            entry = json.loads(line)
            processed_idxes.add(entry["idx"])
            cached_results[entry["idx"]] = entry

    return processed_idxes, cached_results


def get_entries_to_process(
    all_entries: List[Dict[str, Any]],
    processed_idxes: set[int],
) -> list[tuple[int, Dict[str, Any]]]:
    """Prepare entries for processing, filtering out already processed ones."""
    entries_to_process = [
        (i, entry)
        for i, entry in enumerate(all_entries)
        if i not in processed_idxes
    ]

    return entries_to_process


def process_single_entry(
    idx: int,
    entry: Dict[str, Any],
    prompt_template: str,
    cache_filepath: Path,
    log_lock: threading.Lock,
) -> tuple[int, Dict[str, Any]]:
    """Process a single entry and log the result."""
    difficulty = get_difficulty_from_llm(entry["sourceText"], prompt_template)
    entry["llm_difficulty"] = difficulty
    entry["idx"] = idx
    to_cache = json.dumps(entry, ensure_ascii=False)

    with log_lock, open(cache_filepath, "a", encoding="utf-8") as cache_file:
        cache_file.write(f"{to_cache}\n")

    return idx, entry


def process_entries_parallel(
    entries_to_process: List[tuple[int, Dict[str, Any]]],
    prompt_template: str,
    cache_file_path: Path,
    max_workers: int = None,
) -> Dict[int, Dict[str, Any]]:
    """Process entries in parallel using ThreadPoolExecutor."""
    results_map = {}
    log_lock = threading.Lock()

    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        future_to_idx = {
            executor.submit(
                process_single_entry, idx, entry, prompt_template, cache_file_path, log_lock
            ): idx
            for idx, entry in entries_to_process
        }

        for future in as_completed(future_to_idx):
            try:
                idx, result = future.result()
                results_map[idx] = result
            except Exception as e:
                logger.error(
                    "Error processing entry %s: %s",
                    future_to_idx[future],
                    str(e),
                )
                continue

    return results_map


def main(input: str, output: str, source_language: str, translation_language: str):
    """Main function to enrich sentence data."""
    prompt_template = get_prompt_template()
    content = get_content(input)
    data = json.loads(content)
    all_entries = data["sentences"]

    cache_file_path = get_cache_file_path(output)
    processed_idxes, cached_results = load_processed_entries(cache_file_path)
    entries_to_process = get_entries_to_process(all_entries, processed_idxes)

    new_results = process_entries_parallel(
        entries_to_process, prompt_template, cache_file_path
    )
    cached_results.update(new_results)

    enriched_sentences = list(cached_results.values())

    enriched_sentences.sort(key=lambda x: (x["llm_difficulty"], len(x["sourceText"])))

    final_sentences = []
    for i, sentence in enumerate(enriched_sentences):
        final_sentences.append(
            {
                "id": str(uuid.uuid4()),
                "sourceText": sentence["sourceText"],
                "translationText": sentence["translationText"],
                "sourceLanguage": source_language,
                "translationLanguage": translation_language,
                "difficulty": i * 10,
            }
        )

    with get_output_file(output) as f:
        json.dump({"sentences": final_sentences}, f, ensure_ascii=False, indent=2)


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.input, args.output, args.source_language, args.translation_language)
