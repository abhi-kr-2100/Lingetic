import argparse
import asyncio
import json
import logging
import sys
import uuid
from pathlib import Path
from typing import Any, Dict, List, Literal

import requests
from pydantic import BaseModel

from library.errors import InvalidWordIDError
from library.gemini_client import get_global_gemini_client

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

Input: 1: Les, 2: étudiants, 3: étudient, 4: dans, 5: la, 6: bibliothèque

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

Input: 1: Ben, 2: kitabı, 3: okuyorum

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

Input: 1: Jag, 2: vet

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

Input: 1: watashi, 2: wa, 3: eki, 4: e, 5: ikimasu

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


async def make_explanation_api_call(
    prompt: str, request_id: uuid.UUID
) -> Dict[str, Any]:
    client = get_global_gemini_client()
    response = await client.generate_content(
        prompt=prompt, response_schema=ExplanationResult, request_id=request_id
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


async def get_explanation_for_entry(entry: Dict[str, Any]) -> Dict[str, Any]:
    sentence = entry["sourceText"]
    language = entry["sourceLanguage"]
    translation_language = entry["translationLanguage"]

    word_tokens = tokenize_sentence(language, sentence)
    token_str = ", ".join(f"{t['id']}: {t['value']}" for t in word_tokens)

    acceptable_ids = {t["id"] for t in word_tokens}

    prompt = f"""{SYSTEM_PROMPT.format(language=translation_language)}
{EXAMPLES[language]}

Input: {token_str}

Output:"""
    request_id = uuid.uuid5(
        uuid.NAMESPACE_DNS,
        f"explain-sentence-{sentence}-{language}-{translation_language}",
    )
    result = await make_explanation_api_call(prompt, request_id)

    id_to_start_index = {t["id"]: t["startIndex"] for t in word_tokens}
    explanations = result["explanation"]
    for exp in explanations:
        if exp["id"] not in acceptable_ids:
            raise InvalidWordIDError(exp["id"], acceptable_ids)
        exp["startIndex"] = id_to_start_index[exp["id"]]

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


def load_entries(filepath: str) -> List[Dict[str, Any]]:
    """Load entries from file or stdin."""
    if filepath == "-":
        data = json.load(sys.stdin)
    else:
        with open(filepath, "r", encoding="utf-8") as f:
            data = json.load(f)
    return data["sentences"]


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

    async def process_entry(entry, semaphore: asyncio.Semaphore):
        try:
            async with semaphore:
                result = await get_explanation_for_entry(entry)
                return result
        except Exception as e:
            logger.error(
                "Error processing entry '%s': %s", entry["sourceText"], str(e)
            )

    async def async_main():
        all_entries = load_entries(filepath)
        semaphore = asyncio.Semaphore(5)
        tasks = [process_entry(entry, semaphore) for entry in all_entries]
        results = await asyncio.gather(*tasks, return_exceptions=False)

        explained_entries = [result for result in results if result is not None]
        # Remove ID from sourceWordExplanations now because removing it earlier can interfere
        # with Gemini client's cache.
        for entry in explained_entries:
            for exp in entry["sourceWordExplanations"]:
                exp.pop("id", None)

        write_results({"sentences": explained_entries}, output)
        logger.info(
            "Processing complete. Successfully processed %d out of %d entries.",
            len(explained_entries),
            len(all_entries),
        )

    asyncio.run(async_main())


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.filepath, args.output)
