#!/usr/bin/env python

import argparse
import asyncio
import json
import logging
import sys
import uuid
from typing import List, Dict, Any, TextIO, Tuple

import requests
from pydantic import BaseModel

from library.gemini_client import get_global_gemini_client
from library.errors import InvalidWordIDError


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler(sys.stderr)],
)
logger = logging.getLogger(__name__)


PROMPT_TEMPLATE = """Your job is to select the most important words for language learners to focus on from the given sentence.

When selecting words, prioritize:
1. Key vocabulary words that are fundamental to the language
2. Words that are commonly used in everyday conversation
3. Words that might be challenging for language learners
4. Content words (nouns, verbs, adjectives, adverbs) over function words

Example:

Input: 1: Les, 2: étudiants, 3: étudient, 4: dans, 5: la, 6: bibliothèque

Output:
{{ "selectedIds": [2, 6] }}

Reason: Selected "étudiants" (students) and "bibliothèque" (library) as they are content words that are important for language learning.

You must select at least one word!

Sentence: {sentence}
Words: {tokens}
"""


class SelectedWordsResponse(BaseModel):
    selectedIds: List[int]


def load_sentences(filepath: str) -> List[Dict[str, Any]]:
    """
    Load sentences from a JSON file or stdin.
    The file should have a 'sentences' key containing an array of sentence objects.
    """
    if filepath == "-":
        data = json.load(sys.stdin)
    else:
        with open(filepath, "r", encoding="utf-8") as file:
            data = json.load(file)
    return data["sentences"]


def get_output_file(output_path: str) -> TextIO:
    """
    Get the output file handle based on the output path.
    """
    if output_path == "-":
        return sys.stdout
    return open(output_path, "w", encoding="utf-8")


def tokenize_sentences(sentences: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """
    Tokenize all sentences using a single connection pool.
    Returns the sentences with their tokens added.
    """
    session = requests.Session()
    url = "http://localhost:8000/language-service/tokenize"

    sentences_with_tokens = []
    for sentence in sentences:
        text = sentence["sourceText"]
        language = sentence["sourceLanguage"]
        params = {"language": language, "sentence": text}
        response = session.get(url, params=params, timeout=5)
        response.raise_for_status()

        tokens = response.json()
        id_counter = 1
        for token in tokens:
            if token["type"] == "Word":
                token["id"] = id_counter
                id_counter += 1

        sentence_with_tokens = dict(sentence)
        sentence_with_tokens["tokens"] = tokens
        sentences_with_tokens.append(sentence_with_tokens)

    session.close()
    return sentences_with_tokens


async def make_gemini_api_call(
    prompt: str, valid_ids: List[int], request_id: uuid.UUID
) -> List[int]:
    client = get_global_gemini_client()
    response = await client.generate_content(
        prompt=prompt, response_schema=SelectedWordsResponse, request_id=request_id
    )

    ids = response["selectedIds"]
    for id_val in ids:
        if id_val not in valid_ids:
            raise InvalidWordIDError(id_val, valid_ids)

    return ids


def generate_prompt(
    sentence: Dict[str, Any],
    valid_ids: List[int],
) -> str:
    """Generate a prompt for the Gemini API based on the tokens and context."""
    text = sentence["sourceText"]
    tokens = sentence["tokens"]

    prompt = PROMPT_TEMPLATE.format(
        sentence=text,
        tokens=", ".join([
            f"{idx}: {token['value']}"
            for idx, token in enumerate(tokens, start=1)
            if token["type"] == "Word"
        ]),
    )
    prompt += (
        f"You must select one or more IDs from the following: {valid_ids}\n\n"
    )
    prompt += "Output:\n"

    return prompt


def get_word_tokens_with_ids(
    tokens: List[Dict[str, Any]],
) -> Tuple[List[Dict[str, Any]], List[int]]:
    """Extract word tokens and assign IDs to them."""
    word_tokens = [token for token in tokens if token["type"] == "Word"]
    word_tokens_with_ids = []

    for i, token in enumerate(word_tokens, start=1):
        token_copy = dict(token)
        token_copy["id"] = i
        word_tokens_with_ids.append(token_copy)

    valid_ids = [token["id"] for token in word_tokens_with_ids]
    return word_tokens_with_ids, valid_ids


def create_fill_in_blank_question(
    tokens: List[Dict[str, Any]],
    selected_token: Dict[str, Any],
    translation: str,
    language: str,
) -> Dict[str, Any]:
    """Create a fill-in-the-blank question from the selected token."""
    modified_tokens = []
    for token in tokens:
        if token["startIndex"] == selected_token["startIndex"]:
            modified_token = dict(token)
            modified_token["value"] = "_____"
            modified_tokens.append(modified_token)
        else:
            modified_tokens.append(dict(token))

    modified_tokens.sort(key=lambda x: x["startIndex"])

    response = requests.post(
        "http://localhost:8000/language-service/combine-tokens",
        params={"language": language},
        json=modified_tokens,
        timeout=5,
    )
    response.raise_for_status()
    question_text = response.text

    return {
        "question_type": "FillInTheBlanks",
        "question_type_specific_data": {
            "questionText": question_text,
            "answer": selected_token["value"],
            "hint": translation,
        },
    }


async def process_sentence_with_gemini(
    sentence: Dict[str, Any],
) -> Dict[str, Any]:
    """Process a single sentence to generate questions using Gemini API."""
    questions = []
    tokens = sentence["tokens"]
    language = sentence["sourceLanguage"]
    translation_language = sentence["translationLanguage"]

    word_tokens_with_ids, valid_ids = get_word_tokens_with_ids(tokens)

    prompt = generate_prompt(
        sentence,
        valid_ids,
    )

    request_id = uuid.uuid5(
        uuid.NAMESPACE_DNS,
        f'questions-{sentence["sourceText"]}-{language}-{translation_language}',
    )
    selected_ids = await make_gemini_api_call(prompt, valid_ids, request_id)

    seen_words = set()

    for selected_id in selected_ids:
        selected_token = next(
            (t for t in word_tokens_with_ids if t["id"] == selected_id),
            None,
        )

        seen_words.add(selected_token["value"])

        question = create_fill_in_blank_question(
            tokens,
            selected_token,
            sentence["translationText"],
            language,
        )

        questions.append(question)

    sentence_with_questions = dict(sentence)
    sentence_with_questions["questions"] = questions
    return sentence_with_questions


async def process_sentences(
    sentences: List[Dict[str, Any]],
) -> List[Dict[str, Any]]:
    """
    Process sentences in three steps:
    1. Synchronously tokenize all sentences using a connection pool
    2. Process sentences in parallel with Gemini API while maintaining order
    """
    sentences = tokenize_sentences(sentences)

    tasks = [process_sentence_with_gemini(sentence) for sentence in sentences]
    results = await asyncio.gather(*tasks, return_exceptions=True)

    sentences_with_questions = []
    for i, result in enumerate(results):
        if isinstance(result, Exception):
            logger.error(
                "Error processing sentence '%s': %s", sentences[i]["sourceText"], result
            )
        else:
            sentences_with_questions.append(result)

    return sentences_with_questions


def get_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Generate grouped fill-in-the-blank questions from sentences."
    )
    parser.add_argument(
        "filepath",
        nargs="?",
        default="-",
        help="Path to the JSON file containing sentences (default: '-' to read from stdin)",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="-",
        help="Path to the output file (default: '-' for stdout)",
    )
    return parser


def main(filepath: str, output: str) -> None:
    """Main function to process sentences and generate questions."""
    async def async_main():
        sentences = load_sentences(filepath)
        sentences_with_questions = await process_sentences(sentences)

        output_file = get_output_file(output)
        needs_closing = output != "-"
        try:
            json.dump(
                sentences_with_questions, output_file, ensure_ascii=False, indent=2
            )
        finally:
            if needs_closing:
                output_file.close()

    asyncio.run(async_main())


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.filepath, args.output)
