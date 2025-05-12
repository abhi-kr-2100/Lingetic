#!/usr/bin/env python

import argparse
import json
import sys
import os
import time
import random
from typing import List, Dict, Any, TextIO
import requests
from google import genai
from google.genai import types
from google.api_core import exceptions as google_exceptions
from pydantic import BaseModel
from concurrent.futures import ThreadPoolExecutor, as_completed
import threading
from pathlib import Path

# Language-specific prompt examples
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
    """
    try:
        if filepath == "-":
            data = json.load(sys.stdin)
        else:
            with open(filepath, "r", encoding="utf-8") as file:
                data = json.load(file)
        return data.get("data", [])
    except Exception as e:
        print(f"Error loading sentences: {e}", file=sys.stderr)
        sys.exit(1)


def get_output_file(output_path: str) -> TextIO:
    """
    Get the output file handle based on the output path.
    """
    if output_path == "-":
        return sys.stdout
    try:
        return open(output_path, "w", encoding="utf-8")
    except Exception as e:
        print(
            f"Error opening output file '{output_path}': {e}", file=sys.stderr
        )
        sys.exit(1)


def tokenize_sentences(
    sentences: List[Dict[str, Any]], language: str
) -> List[Dict[str, Any]]:
    """
    Tokenize all sentences using a single connection pool.
    Returns the sentences with their tokens added.
    """
    session = requests.Session()
    url = "http://localhost:8000/language-service/tokenize"

    sentences_with_tokens = []
    for sentence in sentences:
        text = sentence.get("text", "")
        try:
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
        except Exception as e:
            print(
                f"Error tokenizing sentence '{text}': {e}; Aborting...",
                file=sys.stderr,
            )
            raise SystemError(f"Tokenization error: {e}")

    session.close()
    return sentences_with_tokens


def make_gemini_api_call(prompt: str) -> List[int]:
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print(
            "Error: GEMINI_API_KEY environment variable not set. "
            "Get an API key at https://ai.google.dev/ and run 'export GEMINI_API_KEY=YOUR_KEY'.",
            file=sys.stderr,
        )
        sys.exit(1)
    try:
        client = genai.Client(api_key=api_key)
    except Exception as e:
        print(f"Error initializing Gemini client: {e}", file=sys.stderr)
        sys.exit(1)

    model_name = "gemini-2.5-flash-preview-04-17"

    # Use Pydantic schema to parse response and configure non-thinking mode
    config = types.GenerateContentConfig(
        response_mime_type="application/json",
        response_schema=SelectedWordsResponse,
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
                model=model_name,
                contents=prompt,
                config=config,
            )
            if hasattr(response, "parsed") and response.parsed is not None:
                parsed = response.parsed.model_dump()
                print(f"Gemini response: {parsed}", file=sys.stderr)
                return parsed["selectedIds"]
            else:
                raw_text = getattr(response, "text", "")
                raise ValueError(
                    f"Gemini response missing parsed output, got: {raw_text}"
                )

        except google_exceptions.ResourceExhausted as err:
            attempt += 1
            if attempt >= max_retries:
                print(
                    f"Error: Reached max retries ({max_retries}) for rate limit error.",
                    file=sys.stderr,
                )
                print(f"Last error: {err}", file=sys.stderr)
                raise err

            delay = base_delay * (2 ** (attempt - 1))
            jitter = random.uniform(0, base_delay)
            time.sleep(delay + jitter)
        except Exception as e:
            print(f"Error calling Gemini API: {e}", file=sys.stderr)
            sys.exit(1)


def generate_prompt(
    sentence: str,
    tokens: List[Dict[str, Any]],
    theme: str,
    instructions: str,
    language: str,
    valid_ids: List[int],
) -> str:
    """Generate a prompt for the Gemini API based on the tokens and context."""
    # Get language-specific examples
    examples = PROMPT_EXAMPLES.get(language)

    # Format the prompt with language-specific examples
    prompt = PROMPT_TEMPLATE.format(**examples)

    # Add the actual query
    prompt += f"\nSentence: {sentence}\nWords: {json.dumps(tokens)}\n\n"
    prompt += f"Theme: {theme}\n"
    prompt += f"Instructions: {instructions}\n\n"
    prompt += (
        f"You must select one or more IDs from the following: {valid_ids}\n\n"
    )
    prompt += "Output:\n"

    return prompt


def process_sentences(
    sentences: List[Dict[str, Any]], language: str, output: str
) -> List[Dict[str, Any]]:
    """
    Process sentences in two steps:
    1. Synchronously tokenize all sentences using a connection pool
    2. Asynchronously process tokens with Gemini API while maintaining order
    """
    # Set log file path for caching
    log_path = (
        Path(output).with_suffix(".log")
        if output != "-"
        else Path("questions_output.log")
    )

    # Add sequential IDs to maintain order
    for i, sentence in enumerate(sentences):
        sentence["_seq_id"] = i

    # Load cache from log file
    processed_ids = set()
    log_results = {}
    if log_path.exists():
        with open(log_path, "r", encoding="utf-8") as logf:
            for line in logf:
                try:
                    entry = json.loads(line)
                    if "_seq_id" in entry:
                        processed_ids.add(entry["_seq_id"])
                        log_results[entry["_seq_id"]] = entry
                except Exception as e:
                    print(f"Error reading log line: {e}", file=sys.stderr)

    # Step 1: Synchronously tokenize all sentences that need processing
    to_process = [
        (i, sent) for i, sent in enumerate(sentences) if i not in processed_ids
    ]
    if to_process:
        sentences_to_tokenize = [sent for _, sent in to_process]
        tokenized_sentences = tokenize_sentences(
            sentences_to_tokenize, language
        )

        # Map tokenized sentences back to their indices
        tokenized_map = {
            i: sent
            for i, sent in zip(
                [idx for idx, _ in to_process], tokenized_sentences
            )
        }
    else:
        tokenized_map = {}

    results_map = {i: log_results[i] for i in processed_ids}
    log_lock = threading.Lock()

    def process_with_gemini(
        idx: int, sentence: Dict[str, Any]
    ) -> Dict[str, Any]:
        text = sentence.get("text", "")
        tokens = sentence.get("tokens", [])
        theme = sentence.get("theme", "")
        instructions = sentence.get("instructions", "")
        translations = sentence.get("translations", [])
        translation = translations[0].get("text", "") if translations else ""

        # Filter word tokens
        word_tokens = [token for token in tokens if token.get("type") == "Word"]
        word_tokens_with_ids = []
        for i, token in enumerate(word_tokens, start=1):
            token_copy = dict(token)
            token_copy["id"] = i
            word_tokens_with_ids.append(token_copy)

        valid_ids = [token["id"] for token in word_tokens_with_ids]

        if not word_tokens:
            questions = []
        else:
            # Generate prompt and get selected tokens from Gemini
            prompt = generate_prompt(
                text,
                word_tokens_with_ids,
                theme,
                instructions,
                language,
                valid_ids,
            )
            try:
                selected_ids = make_gemini_api_call(prompt)
            except Exception as e:
                print(f"Error from Gemini API: {e}", file=sys.stderr)
                selected_ids = [random.choice(valid_ids)]

            # Create questions
            questions = []
            seen_words = set()

            for selected_id in selected_ids:
                selected_token = next(
                    (
                        t
                        for t in word_tokens_with_ids
                        if t.get("id") == selected_id
                    ),
                    None,
                )
                if (
                    selected_token is None
                    or selected_token["value"] in seen_words
                ):
                    print(
                        "Error: Invalid selected token or already seen",
                        file=sys.stderr,
                    )
                    continue

                seen_words.add(selected_token["value"])
                modified_tokens = []
                for token in tokens:
                    if token.get("startIndex") == selected_token["startIndex"]:
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

                questions.append(
                    {
                        "question_type": "FillInTheBlanks",
                        "question_type_specific_data": {
                            "questionText": question_text,
                            "answer": selected_token["value"],
                            "hint": translation,
                        },
                    }
                )

        result = dict(sentence)
        result["questions"] = questions
        # Write to log immediately
        with log_lock:
            with open(log_path, "a", encoding="utf-8") as logf:
                logf.write(json.dumps(result, ensure_ascii=False) + "\n")
        return result

    # Step 2: Asynchronously process tokenized sentences with Gemini
    with ThreadPoolExecutor() as executor:
        future_to_idx = {
            executor.submit(process_with_gemini, idx, tokenized_map[idx]): idx
            for idx in tokenized_map
        }
        for future in as_completed(future_to_idx):
            idx = future_to_idx[future]
            try:
                result = future.result()
                results_map[idx] = result
            except Exception as e:
                print(f"Error processing sentence {idx}: {e}", file=sys.stderr)

    # Reconstruct results in original order and remove _seq_id
    ordered_results = []
    for i in range(len(sentences)):
        if i in results_map:
            result = results_map[i]
            del result["_seq_id"]
            ordered_results.append(result)

    return ordered_results


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
    parser.add_argument(
        "-l",
        "--language",
        required=True,
        help="Language (e.g., French) (case-sensitive!)",
    )
    return parser


def main(filepath: str, output: str, language: str) -> None:
    sentences = load_sentences(filepath)
    objects_with_questions = process_sentences(sentences, language, output)
    output_file = get_output_file(output)
    needs_closing = output != "-"
    try:
        json.dump(
            objects_with_questions, output_file, ensure_ascii=False, indent=2
        )
        output_file.write("\n")
    except Exception as e:
        print(f"Error writing to output: {e}", file=sys.stderr)
        sys.exit(1)
    finally:
        if needs_closing:
            output_file.close()


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.filepath, args.output, args.language)
