import os
import sys
import argparse
import json
import time
import random
from typing import List, Dict, Any
from pathlib import Path
from google import genai
from google.api_core import exceptions as google_exceptions
from pydantic import BaseModel
from scripts.questions import tokenize_sentence
import re
from concurrent.futures import ThreadPoolExecutor, as_completed
import threading

# System prompt and examples for explanation
SYSTEM_PROMPT = """
You're a language teacher who breaks down sentences word-by-word. You'll be given a sentence and your job is to explain the role of each word in the sentence. If applicable, explain the gender, conjugation, tense, etc. Explain why a word takes on the conjugation that it does.
"""

EXAMPLES: Dict[str, str] = {
    "French": """
Example:

Input: 1: Les 2: étudiants 3: étudient 4: dans 5: la 6: bibliothèque

Output:
{
    "explanation": [
        {"sequenceNumber": 1, "word": "Les", "properties": ["article", "plural", "definite"], "comment": "Used because 'étudiants' is plural noun."},
        {"sequenceNumber": 2, "word": "étudiants", "properties": ["noun", "masculine", "plural"], "comment": "Masculine plural noun meaning 'students'."},
        {"sequenceNumber": 3, "word": "étudient", "properties": ["verb", "third-person", "plural", "present"], "comment": "Conjugated to match third-person plural."},
        {"sequenceNumber": 4, "word": "dans", "properties": ["preposition"], "comment": "Preposition meaning 'in'."},
        {"sequenceNumber": 5, "word": "la", "properties": ["article", "singular", "definite", "feminine"], "comment": "Used because 'bibliothèque' is feminine singular noun."},
        {"sequenceNumber": 6, "word": "bibliothèque", "properties": ["noun", "feminine", "singular"], "comment": "Feminine singular noun."}
    ]
}
""",
    "Turkish": """
Example:

Input: 1: Ben 2: kitabı 3: okuyorum

Output:
{
    "explanation": [
        {"sequenceNumber": 1, "word": "Ben", "properties": ["pronoun", "first-person", "singular"], "comment": "Subject pronoun."},
        {"sequenceNumber": 2, "word": "kitabı", "properties": ["noun", "accusative", "singular"], "comment": "Definite object marked with accusative suffix '-ı'."},
        {"sequenceNumber": 3, "word": "okuyorum", "properties": ["verb", "present", "continuous", "first-person", "singular"], "comment": "First-person singular present continuous tense suffix '-yorum'."}
    ]
}
""",
    "Swedish": """
Example:

Input: 1: Jag 2: vet

Output:
{
    "explanation": [
        {"sequenceNumber": 1, "word": "Jag", "properties": ["pronoun", "first-person", "singular"], "comment": "Subject pronoun meaning 'I'."},
        {"sequenceNumber": 2, "word": "vet", "properties": ["verb", "present", "first-person", "singular"], "comment": "Present tense, first-person singular of 'veta' (to know)."}
    ]
}
""",
}


# Pydantic models to constrain AI output
class WordExplanation(BaseModel):
    sequenceNumber: int
    word: str
    properties: List[str]
    comment: str


class ExplanationResult(BaseModel):
    explanation: List[WordExplanation]


def load_schema(schema_path: str) -> List[Dict[str, Any]]:
    """Load and parse the questions JSON file"""
    try:
        with open(schema_path, "r", encoding="utf-8") as f:
            return json.load(f)
    except FileNotFoundError:
        print(f"Error: File '{schema_path}' not found", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"Error: Invalid JSON in '{schema_path}': {e}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"Error loading schema: {e}", file=sys.stderr)
        sys.exit(1)


def make_explanation_api_call(prompt: str) -> Dict[str, Any]:
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print(
            "Error: GEMINI_API_KEY not set. Get an API key and export GEMINI_API_KEY=YOUR_KEY.",
            file=sys.stderr,
        )
        sys.exit(1)

    try:
        client = genai.Client(api_key=api_key)
    except Exception as e:
        print(f"Error initializing Gemini client: {e}", file=sys.stderr)
        sys.exit(1)

    model_name = "gemini-1.5-flash-8b"
    config = {
        "response_mime_type": "application/json",
        "response_schema": ExplanationResult,
    }

    max_retries = 5
    base_delay = 5
    attempt = 0

    while attempt < max_retries:
        try:
            response = client.models.generate_content(
                model=model_name, contents=prompt, config=config
            )
            if hasattr(response, "parsed") and response.parsed is not None:
                return response.parsed.model_dump()
            raw = getattr(response, "text", "[No text]")
            raise ValueError(f"Unexpected response: {raw}")

        except google_exceptions.ResourceExhausted as err:
            attempt += 1
            if attempt >= max_retries:
                print(f"Max retries reached: {err}", file=sys.stderr)
                raise
            delay = base_delay * (2 ** (attempt - 1)) + random.random()
            time.sleep(delay)

        except Exception as e:
            print(f"API call error: {e}", file=sys.stderr)
            raise


def get_explanation_for_entry(entry: Dict[str, Any]) -> Dict[str, Any]:
    language = entry.get("language")
    if language not in EXAMPLES:
        raise ValueError(f"Unsupported language: {language}")

    text = entry["question_type_specific_data"]["questionText"]
    answer = entry["question_type_specific_data"]["answer"]
    sentence = re.sub(r"_+", answer, text)

    # tokenize the sentence and build tokenized input
    tokens = tokenize_sentence(language, sentence)
    word_tokens = [t for t in tokens if t.get("type") == "Word"]
    token_str = " ".join(
        f"{t['sequenceNumber']}: {t['value']}" for t in word_tokens
    )

    prompt = (
        SYSTEM_PROMPT
        + "\n"
        + EXAMPLES[language]
        + f"\n\nInput: {token_str}\n\nOutput:"
    )
    result = make_explanation_api_call(prompt)
    entry["question_type_specific_data"]["explanation"] = result.get(
        "explanation", []
    )
    return entry


def get_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Generate word-by-word explanations for FillInTheBlanks questions"
    )
    parser.add_argument(
        "filepath",
        nargs="?",
        default="-",
        help="Path to questions JSON file, '-' for stdin",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="-",
        help="Output JSON file path, '-' for stdout",
    )
    return parser


def main(filepath: str, output: str) -> None:
    # Set log file path
    log_path = (
        Path(output).with_suffix(".log")
        if output != "-"
        else Path("output.log")
    )

    # Load entries from filepath or stdin
    if filepath == "-":
        entries = json.load(sys.stdin)
    else:
        entries = load_schema(filepath)

    # Load already processed entries from log
    processed_ids = set()
    log_results = {}
    if log_path.exists():
        with open(log_path, "r", encoding="utf-8") as logf:
            for line in logf:
                try:
                    entry = json.loads(line)
                    if "id" in entry:
                        processed_ids.add(entry["id"])
                        log_results[entry["id"]] = entry
                except Exception as e:
                    print(f"Error reading log line: {e}", file=sys.stderr)

    # Prepare only the relevant entries for processing (skip already processed)
    fill_entries = [
        (i, entry)
        for i, entry in enumerate(entries)
        if entry.get("question_type") == "FillInTheBlanks"
        and entry.get("id") not in processed_ids
    ]
    results_map = {
        i: log_results[entry["id"]]
        for i, entry in enumerate(entries)
        if entry.get("id") in processed_ids
    }
    log_lock = threading.Lock()

    def process_and_log(idx, entry):
        result = get_explanation_for_entry(entry)
        # Write to log immediately
        with log_lock:
            with open(log_path, "a", encoding="utf-8") as logf:
                logf.write(json.dumps(result, ensure_ascii=False) + "\n")
        return result

    with ThreadPoolExecutor() as executor:
        future_to_idx = {
            executor.submit(process_and_log, idx, entry): idx
            for idx, entry in fill_entries
        }
        for future in as_completed(future_to_idx):
            idx = future_to_idx[future]
            try:
                result = future.result()
                results_map[idx] = result
            except Exception as e:
                entry_id = entries[idx].get("id")
                print(f"Error on entry {entry_id}: {e}", file=sys.stderr)

    # Reconstruct the results list in input order, skipping non-FillInTheBlanks
    results = []
    for i, entry in enumerate(entries):
        if entry.get("question_type") == "FillInTheBlanks" and i in results_map:
            results.append(results_map[i])

    if output == "-":
        print(json.dumps(results, ensure_ascii=False, indent=2))
    else:
        Path(output).parent.mkdir(parents=True, exist_ok=True)
        with open(output, "w", encoding="utf-8") as f:
            json.dump(results, f, ensure_ascii=False, indent=2)


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.filepath, args.output)
