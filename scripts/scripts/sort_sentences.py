#!/usr/bin/env python

from typing import List, Dict, Any
import signal
import atexit

from functools import cmp_to_key
from sys import stderr, exit, stdout
from json import load, dump
from argparse import ArgumentParser
from pathlib import Path

from ollama import chat


class CacheManager:
    _instance = None

    def __init__(self):
        self.cache = {}
        self.cache_file = None

    @classmethod
    def get_instance(cls):
        if cls._instance is None:
            cls._instance = CacheManager()
        return cls._instance

    def set_cache_file(self, path: str):
        self.cache_file = path
        if path:
            self.cache = load_cache(path)

    def get_cache(self):
        return self.cache

    def save(self):
        if self.cache_file:
            save_cache(self.cache_file, self.cache)


def save_cache(cache_file: str, cache: Dict) -> None:
    """
    Save the comparison cache to a file.

    Args:
        cache_file: Path to the cache file
        cache: Cache dictionary to save
    """
    if not cache_file:
        return

    try:
        # Convert tuple keys to strings for JSON compatibility
        serializable_cache = {
            f"{s1}|||{s2}": result for (s1, s2), result in cache.items()
        }
        with open(cache_file, "w", encoding="utf-8") as f:
            dump(serializable_cache, f, ensure_ascii=False, indent=2)
    except Exception as e:
        print(f"Error saving cache: {e}", file=stderr)


def load_cache(cache_file: str) -> Dict:
    """
    Load the comparison cache from a file.

    Args:
        cache_file: Path to the cache file

    Returns:
        Dictionary containing cached comparisons
    """
    if not cache_file or not Path(cache_file).exists():
        return {}

    try:
        with open(cache_file, "r", encoding="utf-8") as f:
            serialized_cache = load(f)
            print("INFO: Loaded cache from file.", file=stderr)
            # Convert string keys back to tuples
            return {
                tuple(key.split("|||")): value
                for key, value in serialized_cache.items()
            }
    except Exception as e:
        print(f"Error loading cache: {e}", file=stderr)
        return {}


def signal_handler(signum, frame):
    """Handle interrupt signal by saving cache before exit"""
    print("\nInterrupted. Saving cache...", file=stderr)
    CacheManager.get_instance().save()
    exit(1)


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

Consider the following factors in detail:
1. Vocabulary Complexity:
   - Word frequency and commonness in everyday language
   - Abstract vs concrete words
   - Technical or specialized terminology
   - Number of cognates with English
   - Word length and syllable count
   - Presence of compound words
   - Register (formal vs informal language)
   - False friends with English

2. Grammatical Elements:
   - Verb tense complexity (simple present vs subjunctive, etc.)
   - Number of clauses and subordination levels
   - Complex grammatical structures (subjunctive, conditionals, etc.)
   - Word order complexity
   - Agreement complexity (gender, number, etc.)
   - Voice (active vs passive)
   - Mood (indicative, subjunctive, imperative)
   - Modal verb usage

3. Sentence Structure:
   - Overall length and word count
   - Number of dependent and independent clauses
   - Presence of embedded phrases
   - Complexity of punctuation
   - Coordination vs subordination
   - Left vs right branching structures
   - Parenthetical expressions
   - Distance between related elements

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
        comparison_result = -1
    elif "SECOND_EASIER" in result:
        print(f"INFO: {sentence2['text']}", file=stderr)
        comparison_result = 1
    else:
        comparison_result = 0

    return comparison_result


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
    cache_manager = CacheManager.get_instance()
    comparison_cache = cache_manager.get_cache()

    def cached_compare(s1, s2):
        # Create a unique key for this comparison pair
        # Use sentence IDs or text as keys
        key = (s1["text"], s2["text"])
        reverse_key = (s2["text"], s1["text"])

        if key in comparison_cache:
            return comparison_cache[key]
        elif reverse_key in comparison_cache:
            return -comparison_cache[reverse_key]

        # Perform the comparison and cache the result
        result = compare_sentence_difficulty(s1, s2)
        comparison_cache[key] = result
        return result

    # Create a custom key function for sorting with caching
    difficulty_key = cmp_to_key(cached_compare)

    # Sort the sentences using the custom comparator
    return sorted(sentences, key=difficulty_key)


def main(file_path: str, output: str = "-", cache: str = None) -> None:
    """
    Load sentences from a file, sort them by difficulty, and output to file or stdout.

    Args:
        file_path: Path to the JSON file containing sentences
        output: Output file path or "-" for stdout (default: "-")
        cache: Path to the cache file (default: None)
    """
    cache_manager = CacheManager.get_instance()
    cache_manager.set_cache_file(cache)

    if cache:
        signal.signal(signal.SIGINT, signal_handler)
        atexit.register(cache_manager.save)

    # Handle file output
    if output == "-":
        output_file = stdout
    else:
        output_file = open(output, "w", encoding="utf-8")

    try:
        sentences = load_sentences(file_path)["data"]

        # Use length as a heuristic to sort sentences by difficulty. Use AI to sort
        # further. Presorting aims to reduce the amount of work AI has to do.
        presorted = sort_by_length(sentences)
        sorted_sentences = sort_sentences_by_difficulty(presorted)

        dump(
            {"data": sorted_sentences},
            output_file,
            ensure_ascii=False,
            indent=2,
        )
    finally:
        if output != "-":
            output_file.close()
        if cache:
            cache_manager.save()


def get_parser() -> ArgumentParser:
    parser = ArgumentParser(description="Sort sentences by difficulty using AI")
    parser.add_argument(
        "file_path", help="Path to the JSON file containing sentences"
    )
    parser.add_argument(
        "--output",
        "-o",
        type=str,
        default="-",
        help="Output file path (default: stdout)",
    )
    parser.add_argument(
        "--cache",
        "-c",
        type=str,
        help="Path to cache file for storing comparison results",
    )
    return parser


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.file_path, args.output, args.cache)
