import os
import sys
import argparse
import json
import time
import random
from typing import List, Dict, Any, Literal
from pathlib import Path
from google import genai
from google.genai import types
from google.api_core import exceptions as google_exceptions
from pydantic import BaseModel
import re
from concurrent.futures import ThreadPoolExecutor, as_completed
import threading
import requests

# System prompt and examples for explanation
SYSTEM_PROMPT = """
You're a language teacher who breaks down sentences word-by-word. You'll be given a sentence and your job is to explain the role of each word in the sentence. Make sure to explain the meaning of the word. If applicable, explain the gender, conjugation, tense, etc. Explain why a word takes on the conjugation that it does.
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


# Pydantic models to constrain AI output
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

    model_name = "gemini-2.5-flash-preview-04-17"

    config = types.GenerateContentConfig(
        response_mime_type="application/json",
        response_schema=ExplanationResult,
        thinking_config=types.ThinkingConfig(
            thinking_budget=0,
        ),
    )

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


def tokenize_sentence(language: str, sentence: str) -> List[Dict[str, Any]]:
    """
    Tokenize a sentence using the language service.
    Returns the tokens from the service response.
    """
    try:
        url = "http://localhost:8000/language-service/tokenize"
        params = {"language": language, "sentence": sentence}
        response = requests.get(url, params=params, timeout=5)
        response.raise_for_status()

        tokens = response.json()
        id = 1
        for token in tokens:
            if token["type"] == "Word":
                token["id"] = id
                id += 1
        return tokens
    except Exception as e:
        print(f"Error tokenizing sentence '{sentence}': {e}", file=sys.stderr)
        raise SystemError(f"Tokenization error: {e}")


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
        f"{i}: {t['value']}" for (i, t) in enumerate(word_tokens, start=1)
    )

    prompt = (
        SYSTEM_PROMPT
        + "\n"
        + EXAMPLES[language]
        + f"\n\nInput: {token_str}\n\nOutput:"
    )
    result = make_explanation_api_call(prompt)

    # Update each explanation with startIndex from the tokens
    token_map = {
        i + 1: t.get("startIndex", 0) for i, t in enumerate(word_tokens)
    }
    explanations = result.get("explanation", [])
    for exp in explanations:
        exp["startIndex"] = token_map.get(exp["id"], 0)

    entry["question_type_specific_data"]["explanation"] = explanations
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
