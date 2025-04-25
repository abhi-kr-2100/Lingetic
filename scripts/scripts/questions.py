#!/usr/bin/env python

import argparse
import json
import sys
from typing import List, Dict, Any, TextIO
import requests


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
        print(f"Error opening output file '{output_path}': {e}", file=sys.stderr)
        sys.exit(1)


def tokenize_sentence(language: str, sentence: str) -> List[Dict[str, Any]]:
    """
    Tokenize a sentence using the local HTTP API.
    """
    url = "http://localhost:8000/language-service/tokenize"
    params = {"language": language, "sentence": sentence}
    try:
        response = requests.get(url, params=params, timeout=5)
        response.raise_for_status()
        return response.json()
    except Exception as e:
        print(f"Error tokenizing sentence '{sentence}': {e}", file=sys.stderr)
        return []


def generate_fill_in_the_blank_questions(sentence_obj: Dict[str, Any], language: str) -> List[Dict[str, Any]]:
    """
    Generate all possible fill-in-the-blank questions for a sentence, one for each Word token.
    """
    text = sentence_obj.get("text", "")
    translations = sentence_obj.get("translations", [])
    translation = translations[0].get("text", "") if translations else ""
    tokens = tokenize_sentence(language, text)

    # Find indices and values of Word tokens
    word_indices = [(i, t["value"]) for i, t in enumerate(tokens) if t.get("type") == "Word"]
    questions = []
    for idx, word in word_indices:
        # Reconstruct the sentence with the idx-th Word replaced by blanks
        masked_tokens = tokens.copy()
        masked_tokens[idx] = {**masked_tokens[idx], "value": "_____"}
        # Rebuild the sentence, preserving original spacing/punctuation
        question_text = ""
        prev_end = 0
        for t in masked_tokens:
            val = t["value"]
            if question_text and not question_text.endswith(" ") and t["type"] == "Word":
                question_text += " "
            question_text += val
        questions.append({
            "question_type": "FillInTheBlanks",
            "question_type_specific_data": {
                "questionText": question_text,
                "answer": word,
                "hint": translation,
            },
        })
    return questions


def process_sentences(sentences: List[Dict[str, Any]], language: str) -> List[Dict[str, Any]]:
    """
    For each sentence, generate an object with a 'questions' key listing all fill-in-the-blank questions.
    Preserve any extra keys from the input entry.
    """
    result = []
    for sentence in sentences:
        questions = generate_fill_in_the_blank_questions(sentence, language)
        # Copy all keys from the input entry
        obj = dict(sentence)
        obj["questions"] = questions
        result.append(obj)
    return result


def get_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Generate grouped fill-in-the-blank questions from sentences.")
    parser.add_argument(
        "filepath",
        nargs="?",
        default="-",
        help="Path to the JSON file containing sentences (default: '-' to read from stdin)",
    )
    parser.add_argument(
        "-o", "--output", default="-", help="Path to the output file (default: '-' for stdout)"
    )
    parser.add_argument(
        "-l", "--language", required=True, help="Language (e.g., French, Turkish) (case-sensitive!)"
    )
    return parser


def main(filepath: str, output: str, language: str) -> None:
    sentences = load_sentences(filepath)
    objects_with_questions = process_sentences(sentences, language)
    output_file = get_output_file(output)
    needs_closing = output != "-"
    try:
        json.dump(objects_with_questions, output_file, ensure_ascii=False, indent=2)
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
