#!/usr/bin/env python

from typing import List, Dict, Any

from functools import cmp_to_key
from sys import stderr, exit, stdout
from json import load, dump
from argparse import ArgumentParser

from ollama import chat


def sort_by_length(sentences: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """
    Sort sentences by their text length (shortest to longest).

    Args:
        sentences: List of sentence dictionaries

    Returns:
        Sorted list of sentence dictionaries
    """
    return sorted(sentences, key=lambda sentence: len(sentence["text"]))


def load_sentences(file_path: str) -> List[Dict[str, Any]]:
    """
    Load sentences from a JSON file.

    Args:
        file_path: Path to the JSON file containing sentences

    Returns:
        A list of dictionaries containing sentence data
    """
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            return load(f)
    except Exception as e:
        print(f"Error loading sentences: {e}", file=stderr)
        exit(1)


def compare_sentence_difficulty(
    sentence1: Dict[str, Any], sentence2: Dict[str, Any]
) -> int:
    """
    Compare two sentences to determine which is more difficult using Ollama.

    Args:
        sentence1: First sentence dictionary
        sentence2: Second sentence dictionary

    Returns:
        -1 if sentence1 is easier, 1 if sentence2 is easier, 0 if equal difficulty
    """
    prompt = f"""
Compare these two sentences and determine which one is easier to understand for a language learner.
Sentence 1: "{sentence1["text"]}"
Sentence 2: "{sentence2["text"]}"

Consider factors like vocabulary complexity, grammatical structures, idioms, and sentence length.
Return ONLY ONE of these exact responses:
- "FIRST_EASIER" if the first sentence is easier
- "SECOND_EASIER" if the second sentence is easier
- "EQUAL" if they are approximately equal in difficulty
"""

    response = chat(
        model="gemma3", messages=[{"role": "user", "content": prompt}]
    )

    result = response["message"]["content"].strip()

    if "FIRST_EASIER" in result and "SECOND_EASIER" in result:
        print("WARNING: Comparison failed.", file=stderr)

    if "FIRST_EASIER" in result:
        print(f"INFO: {sentence1['text']}", file=stderr)
        return -1
    elif "SECOND_EASIER" in result:
        print(f"INFO: {sentence2['text']}", file=stderr)
        return 1
    else:
        return 0


def sort_sentences_by_difficulty(
    sentences: List[Dict[str, Any]],
) -> List[Dict[str, Any]]:
    """
    Sort sentences from easiest to most difficult using AI-powered comparison.

    Args:
        sentences: List of sentence dictionaries

    Returns:
        Sorted list of sentence dictionaries
    """
    # Create a custom key function for sorting
    difficulty_key = cmp_to_key(
        lambda s1, s2: compare_sentence_difficulty(s1, s2)
    )

    # Sort the sentences using the custom comparator
    return sorted(sentences, key=difficulty_key)


def main(file_path: str) -> None:
    """
    Load sentences from a file, sort them by difficulty, and output to stdout.

    Args:
        file_path: Path to the JSON file containing sentences
    """
    sentences = load_sentences(file_path)["data"]

    # Use length as a heuristic to sort sentences by difficulty. Use AI to sort
    # further. Presorting aims to reduce the amount of work AI has to do.
    presorted = sort_by_length(sentences)
    sorted_sentences = sort_sentences_by_difficulty(presorted)

    dump(sorted_sentences, stdout, ensure_ascii=False, indent=2)


if __name__ == "__main__":
    parser = ArgumentParser(description="Sort sentences by difficulty using AI")
    parser.add_argument(
        "file_path", help="Path to the JSON file containing sentences"
    )
    args = parser.parse_args()

    main(args.file_path)
