#!/usr/bin/env python

import argparse
import concurrent.futures
import json
import logging
import sys
from typing import List, Dict, Any, TextIO, Tuple, Set
import requests
from pydantic import BaseModel
import threading
from pathlib import Path

from library.gemini_client import get_global_gemini_client
from scripts.explanations import InvalidWordIDError


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler(sys.stderr)],
)
logger = logging.getLogger(__name__)


PROMPT_EXAMPLES = {
    "French": {
        "sentence": "Les étudiants étudient dans la bibliothèque",
        "word": "dans",
        "words": ["étudiants", "bibliothèque"],
        "blank1": "Les étudiants étudient ____ la bibliothèque",
        "blanks": [
            "Les _____ étudient dans la bibliothèque",
            "Les étudiants étudient dans la _____",
        ],
        "example": """
Example:
Sentence: "Le supermarché est à gauche."
Words: [{{"value":"Le","id":1}}, {{"value":"supermarché","id":2}}, {{"value":"est","id":3}}, {{"value":"à","id":4}}, {{"value":"gauche","id":5}}]

Theme: Asking for Directions and Getting Around
Instructions: Teach phrases for asking directions (Où est...?, Comment aller à...?) and basic directions (tout droit, à gauche, à droite). Introduce basic imperative forms (Allez...) and prepositions of direction. Average sentence length: 5 words.

Output:
{{ "selectedIds": [4, 5] }}

Reason: Both words `à` and `gauche` are relevant to the theme and instructions.""",
    },
    "Swedish": {
        "sentence": "Studenten läser en bok i biblioteket",
        "word": "bok",
        "words": ["studenten", "biblioteket"],
        "blank1": "Studenten läser en ____ i biblioteket",
        "blanks": [
            "_____ läser en bok i biblioteket",
            "Studenten läser en bok i _____",
        ],
        "example": """
Example:
Sentence: "Studenterna läser i biblioteket."
Words: [{{"value":"Studenten","id":1}},{{"value":"läser","id":2}},{{"value":"en","id":3}},{{"value":"bok","id":4}},{{"value":"i","id":5}},{{"value":"biblioteket","id":6}}]

Theme: Objects and nouns
Instructions: Teach nouns that are basic everyday objects. Average sentence length: 5 words.

Output:
{{ "selectedIds": [4, 6] }}

Reason: Both words `bok` and `biblioteket` are relevant to the theme and instructions. The word `Studenten` is not relevant to the theme and instructions, as it is a proper noun and does not fit the context of basic everyday objects.
        """,
    },
    "JapaneseModifiedHepburn": {
        "sentence": "gakusei wa toshokan de benkyou shimasu",
        "word": "de",
        "words": ["gakusei", "toshokan"],
        "blank1": "gakusei wa toshokan ____ benkyou shimasu",
        "blanks": [
            "_____ wa toshokan de benkyou shimasu",
            "gakusei wa _____ de benkyou shimasu",
        ],
        "example": """
Example:
Sentence: "Watashi wa eki e ikimasu."
Words: [{{"value":"watashi","id":1}},{{"value":"wa","id":2}},{{"value":"eki","id":3}},{{"value":"e","id":4}},{{"value":"ikimasu","id":5}}]

Theme: Basic Locations and Transportation
Instructions: Teach basic location particles (e, ni, de) and verbs of motion (iku, kuru). Focus on simple sentence structures using basic particles. Average sentence length: 3-5 words.

Output:
{{ "selectedIds": [3, 4] }}

Reason: Both words `eki` (station) and `e` (direction particle) are relevant to the theme and instructions, as they deal with locations and particles used for indicating direction.
        """,
    },
}

PROMPT_TEMPLATE = """Your job is to create fill-in-the-blank questions. To create a fill-in-the-blank question, start with a set of words: "{sentence}" Then choose a word to hide, say, "{word}". The fill-in-the-blank question becomes: "{blank1}"

You can also select multiple words: "{words[0]}", "{words[1]}". In this case, two fill-in-the-blanks can be created:

* {blanks[0]}
* {blanks[1]}

You'll be given a sentence, and you have to select a word to make a fill-in-the-blank question. You should decide which word to select based on the given theme and instructions to help a language learner in his practice.

{example}

You must select at least one word!
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
        id = 1
        for token in tokens:
            if token["type"] == "Word":
                token["id"] = id
                id += 1

        sentence_with_tokens = dict(sentence)
        sentence_with_tokens["tokens"] = tokens
        sentences_with_tokens.append(sentence_with_tokens)

    session.close()
    return sentences_with_tokens


def make_gemini_api_call(prompt: str, valid_ids: List[int]) -> List[int]:
    client = get_global_gemini_client()
    response = client.generate_content(
        prompt=prompt, response_schema=SelectedWordsResponse
    )

    ids = response["selectedIds"]
    for id in ids:
        if id not in valid_ids:
            raise InvalidWordIDError(id, valid_ids)

    return ids


def generate_prompt(
    sentence: Dict[str, Any],
    valid_ids: List[int],
) -> str:
    """Generate a prompt for the Gemini API based on the tokens and context."""
    language = sentence["sourceLanguage"]
    examples = PROMPT_EXAMPLES[language]

    text = sentence["sourceText"]
    tokens = sentence["tokens"]
    theme = sentence["theme"]
    instructions = sentence["instructions"]

    prompt = PROMPT_TEMPLATE.format(**examples)
    prompt += f"\nSentence: {text}\nWords: {json.dumps(tokens)}\n\n"
    prompt += f"Theme: {theme}\n"
    prompt += f"Instructions: {instructions}\n\n"
    prompt += (
        f"You must select one or more IDs from the following: {valid_ids}\n\n"
    )
    prompt += "Output:\n"

    return prompt


def load_cached_results(
    cache_path: Path,
) -> Tuple[Set[int], Dict[int, Dict[str, Any]]]:
    """Load cached results from file if it exists."""
    processed_idxes = set()
    cached_results = {}

    if not cache_path.exists():
        return processed_idxes, cached_results

    with open(cache_path, "r", encoding="utf-8") as cache_file:
        for line in cache_file:
            entry = json.loads(line)
            idx = entry["idx"]
            processed_idxes.add(idx)
            cached_results[idx] = entry

    return processed_idxes, cached_results


def prepare_sentences_for_processing(
    sentences: List[Dict[str, Any]],
) -> List[Tuple[int, Dict[str, Any]]]:
    """Prepare sentences for processing by adding sequence IDs."""
    return [(i, sentence) for i, sentence in enumerate(sentences)]


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


def process_sentence_with_gemini(
    sentence: Dict[str, Any],
) -> List[Dict[str, Any]]:
    """Process a single sentence to generate questions using Gemini API."""
    questions = []
    tokens = sentence["tokens"]
    language = sentence["sourceLanguage"]

    word_tokens_with_ids, valid_ids = get_word_tokens_with_ids(tokens)

    prompt = generate_prompt(
        sentence,
        valid_ids,
    )

    selected_ids = make_gemini_api_call(prompt, valid_ids)

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

    return questions


def save_to_cache(
    cache_path: Path, result: Dict[str, Any], lock: threading.Lock
):
    """Save processed result to cache file in a thread-safe manner."""
    cache_entry = {**result}

    with lock:
        with open(cache_path, "a", encoding="utf-8") as cache_file:
            cache_file.write(json.dumps(cache_entry, ensure_ascii=False) + "\n")


def process_sentence_with_cache(
    idx: int,
    sentence: Dict[str, Any],
    cache_path: Path,
    cache_file_lock: threading.Lock,
    cached_results: Dict[int, Dict[str, Any]],
) -> None:
    """Process a single sentence and update the cache with the result."""
    questions = process_sentence_with_gemini(sentence)
    result = {"questions": questions}
    save_to_cache(cache_path, result, cache_file_lock)
    cached_results[idx] = result


def process_sentences(
    sentences: List[Dict[str, Any]],
    output: str,
) -> List[Dict[str, Any]]:
    """
    Process sentences in three steps:
    1. Synchronously tokenize all sentences using a connection pool
    2. Process sentences in parallel with Gemini API while maintaining order
    3. Directly handle SourceToTargetTranslation questions without Gemini API
    """
    cache_path = (
        Path(output).with_suffix(".cache")
        if output != "-"
        else Path("output.cache")
    )
    cache_file_lock = threading.Lock()
    processed_idxes, cached_results = load_cached_results(cache_path)

    sentences = tokenize_sentences(sentences)
    sentences_with_idxes = prepare_sentences_for_processing(sentences)
    to_process = [
        (i, sent)
        for i, sent in sentences_with_idxes
        if i not in processed_idxes
    ]

    with concurrent.futures.ThreadPoolExecutor() as executor:
        future_to_idx = {
            executor.submit(
                process_sentence_with_cache,
                idx,
                sent,
                cache_path,
                cache_file_lock,
                cached_results,
            ): idx
            for idx, sent in to_process
        }

        for future in concurrent.futures.as_completed(future_to_idx):
            idx = future_to_idx[future]
            try:
                future.result()
            except InvalidWordIDError as e:
                logger.error(f"Error processing sentence at index {idx}: {e}")

    return [
        {
            **sent,
            "questions": cached_results[i]["questions"]
            if i in cached_results
            else [],
        }
        for i, sent in enumerate(sentences)
    ]


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
    sentences = load_sentences(filepath)
    sentences_with_questions = process_sentences(sentences, output)

    output_file = get_output_file(output)
    needs_closing = output != "-"
    try:
        json.dump(
            sentences_with_questions, output_file, ensure_ascii=False, indent=2
        )
    finally:
        if needs_closing:
            output_file.close()


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.filepath, args.output)
