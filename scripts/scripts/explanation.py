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
    entry["explanation"] = result.get("explanation", [])
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
    # Load entries from filepath or stdin
    if filepath == "-":
        entries = json.load(sys.stdin)
    else:
        entries = load_schema(filepath)
    results: List[Dict[str, Any]] = []
    for entry in entries:
        if entry.get("question_type") != "FillInTheBlanks":
            continue
        try:
            results.append(get_explanation_for_entry(entry))
        except Exception as e:
            print(f"Error on entry {entry.get('id')}: {e}", file=sys.stderr)

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
