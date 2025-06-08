import argparse
import json
import logging
import sys
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Any, Dict, List, Literal

import requests
from library.gemini_client import get_global_gemini_client
from pydantic import BaseModel

from library.errors import InvalidWordIDError

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler(sys.stderr)],
)
logger = logging.getLogger(__name__)


SYSTEM_PROMPT = """
You're a language teacher who breaks down sentences word-by-word. You'll be given a sentence and your job is to explain the role of each word in the sentence. Make sure to explain the meaning of the word. If applicable, explain the gender, conjugation, tense, etc. Explain why a word takes on the conjugation that it does.

Output the explanation in the {language} language.
"""


EXAMPLES: Dict[str, str] = {
    "French": """
Example:

Input: 1: Les 2: étudiants 3: étudient 4: dans 5: la 6: bibliothèque

Output:
{
    "explanation": [
        {"id": 1, "word": "Les", "properties": ["article", "plural", "definite"], "comment": "'Les' means 'the (for plural, male or mixed gender)'. Used because 'étudiants' is plural noun."},
        {"id": 2, "word": "étudiants", "properties": ["noun", "masculine", "plural"], "comment": "Masculine plural noun meaning 'students' (male, or mixed gender)."},
        ...
    ]
}
""",
    "Turkish": """
Example:

Input: 1: Ben 2: kitabı 3: okuyorum

Output:
{
    "explanation": [
        {"id": 1, "word": "Ben", "properties": ["pronoun", "first-person", "singular"], "comment": "Subject pronoun meaning 'I'."},
        {"id": 2, "word": "kitabı", "properties": ["noun", "accusative", "singular"], "comment": "Definite object marked with accusative suffix '-ı'. Means book."},
        {"id": 3, "word": "okuyorum", "properties": ["verb", "present", "continuous", "first-person", "singular"], "comment": "First-person singular present continuous tense suffix '-yorum'. Means 'I am reading'."}
    ]
}
""",
    "Swedish": """
Example:

Input: 1: Jag 2: vet

Output:
{
    "explanation": [
        {"id": 1, "word": "Jag", "properties": ["pronoun", "first-person", "singular"], "comment": "Subject pronoun meaning 'I'."},
        {"id": 2, "word": "vet", "properties": ["verb", "present", "first-person", "singular"], "comment": "Present tense, first-person singular of 'veta' (to know)."}
    ]
}
""",
    "JapaneseModifiedHepburn": """
Example:

Input: 1: watashi 2: wa 3: eki 4: e 5: ikimasu

Output:
{
    "explanation": [
        {"id": 1, "word": "watashi", "properties": ["pronoun", "first-person", "singular"], "comment": "Subject pronoun meaning 'I' (polite/neutral)"},
        {"id": 2, "word": "wa", "properties": ["particle", "topic"], "comment": "Topic marker particle that marks 'watashi' as the topic of the sentence"},
        {"id": 3, "word": "eki", "properties": ["noun", "singular"], "comment": "Means 'station'. Common location noun."},
        {"id": 4, "word": "e", "properties": ["particle", "direction"], "comment": "Directional particle indicating movement towards 'eki' (the station)"},
        {"id": 5, "word": "ikimasu", "properties": ["verb", "present", "polite", "first-person", "singular"], "comment": "Polite form of verb 'iku' (to go). -masu ending indicates polite present tense."}
    ]
}
""",
}


class WordExplanation(BaseModel):
    id: int
    word: str
    startIndex: int  # Add startIndex field
    properties: List[
        Literal[
            "adjective",
            "adverb",
            "conjunction",
            "determiner",
            "interjection",
            "noun",
            "preposition",
            "pronoun",
            "subordinator",
            "verb",
            "article",
            "copula",
            "infinitive",
            "masculine",
            "feminine",
            "neuter",
            "plural",
            "singular",
            "nominative",
            "accusative",
            "dative",
            "ablative",
            "genitive",
            "vocative",
            "locative",
            "instrumental",
            "elative",
            "comitative",
            "privative",
            "present",
            "past",
            "future",
            "imperfect",
            "perfect",
            "future perfect",
            "pluperfect",
            "formal",
            "casual",
        ]
    ]
    comment: str


class ExplanationResult(BaseModel):
    explanation: List[WordExplanation]


def make_explanation_api_call(prompt: str) -> Dict[str, Any]:
    client = get_global_gemini_client()
    response = client.generate_content(
        prompt=prompt, response_schema=ExplanationResult
    )

    return response


def tokenize_sentence(language: str, sentence: str) -> List[Dict[str, Any]]:
    """
    Tokenize a sentence using the language service.
    Returns the tokens from the service response.
    """
    url = "http://localhost:8000/language-service/tokenize"
    params = {"language": language, "sentence": sentence}
    response = requests.get(url, params=params, timeout=5)
    response.raise_for_status()

    tokens = response.json()
    word_tokens = []
    id = 1
    for token in tokens:
        if token["type"] == "Word":
            token["id"] = id
            id += 1
            word_tokens.append(token)
    return word_tokens


def get_explanation_for_entry(entry: Dict[str, Any]) -> Dict[str, Any]:
    sentence = entry["sourceText"]
    language = entry["sourceLanguage"]
    translation_language = entry["translationLanguage"]

    word_tokens = tokenize_sentence(language, sentence)
    token_str = " ".join(f"{t['id']}: {t['value']}" for t in word_tokens)

    acceptable_ids = {t["id"] for t in word_tokens}

    prompt = f"{SYSTEM_PROMPT.format(language=translation_language)}\n{EXAMPLES[language]}\n\nInput: {token_str}\n\nOutput:"
    result = make_explanation_api_call(prompt)

    id_to_start_index = {t["id"]: t["startIndex"] for t in word_tokens}
    explanations = result["explanation"]
    for exp in explanations:
        if exp["id"] not in acceptable_ids:
            raise InvalidWordIDError(exp["id"], acceptable_ids)
        exp["startIndex"] = id_to_start_index[exp["id"]]
        del exp["id"]

    entry["sourceWordExplanations"] = explanations
    return entry


def get_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Generate word-by-word explanations for sentences"
    )
    parser.add_argument(
        "filepath",
        nargs="?",
        default="-",
        help="Path to sentences JSON file, '-' for stdin",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="-",
        help="Output JSON file path, '-' for stdout",
    )
    return parser


def get_cache_file_path(output: str) -> Path:
    """Determine the cache file path based on the output path."""
    return (
        Path(output).with_suffix(".cache")
        if output != "-"
        else Path("output.cache")
    )


def load_entries(filepath: str) -> List[Dict[str, Any]]:
    """Load entries from file or stdin."""
    if filepath == "-":
        data = json.load(sys.stdin)
    else:
        with open(filepath, "r", encoding="utf-8") as f:
            data = json.load(f)
    return data["sentences"]


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
    cache_filepath: Path,
    log_lock: threading.Lock,
) -> tuple[int, Dict[str, Any]]:
    """Process a single entry and log the result."""
    result = get_explanation_for_entry(entry)
    result["idx"] = idx
    to_cache = json.dumps(result, ensure_ascii=False)

    with log_lock, open(cache_filepath, "a", encoding="utf-8") as cache_file:
        cache_file.write(f"{to_cache}\n")

    return idx, result


def process_entries_parallel(
    entries_to_process: List[tuple[int, Dict[str, Any]]],
    cache_file_path: Path,
    max_workers: int = None,
) -> Dict[int, Dict[str, Any]]:
    """Process entries in parallel using ThreadPoolExecutor."""
    results_map = {}
    log_lock = threading.Lock()

    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        future_to_idx = {
            executor.submit(
                process_single_entry, idx, entry, cache_file_path, log_lock
            ): idx
            for idx, entry in entries_to_process
        }

        for future in as_completed(future_to_idx):
            try:
                idx, result = future.result()
            except InvalidWordIDError as e:
                logger.error(
                    "Error processing entry %s: %s",
                    future_to_idx[future],
                    str(e),
                )
                continue
            results_map[idx] = result

    return results_map


def generate_final_results(
    entries: List[Dict[str, Any]], results_map: Dict[int, Dict[str, Any]]
) -> Dict[str, List[Dict[str, Any]]]:
    """Generate final results list in original order."""
    return {
        "sentences": [
            results_map[i]
            for i in range(len(entries))
            # if i is not in results_map, it may have been skipped due to an error
            if i in results_map
        ]
    }


def write_results(
    results: Dict[str, List[Dict[str, Any]]], output: str
) -> None:
    """Write results to output file or stdout."""
    if output == "-":
        print(json.dumps(results, ensure_ascii=False, indent=2))
    else:
        output_path = Path(output)
        output_path.parent.mkdir(parents=True, exist_ok=True)
        with open(output_path, "w", encoding="utf-8") as f:
            json.dump(results, f, ensure_ascii=False, indent=2)


def main(filepath: str, output: str) -> None:
    """Main function to process language entries and generate explanations."""
    cache_file_path = get_cache_file_path(output)
    all_entries = load_entries(filepath)

    processed_idxes, cached_results = load_processed_entries(cache_file_path)
    entries_to_process = get_entries_to_process(all_entries, processed_idxes)

    new_results = process_entries_parallel(entries_to_process, cache_file_path)
    cached_results.update(new_results)

    final_results = generate_final_results(all_entries, cached_results)
    write_results(final_results, output)


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.filepath, args.output)
