#!/usr/bin/env python

from typing import Dict, List, Any
import json
import sys
import random
import argparse
import re


def load_sentences(filepath: str) -> List[Dict[str, str]]:
    """
    Load sentences from a JSON file.

    Args:
        filepath: Path to the JSON file containing sentences

    Returns:
        A list of dictionaries with text and translation fields
    """
    try:
        with open(filepath, "r", encoding="utf-8") as file:
            data = json.load(file)
            return data.get("data", [])
    except FileNotFoundError:
        print(f"Error: File '{filepath}' not found", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError:
        print(
            f"Error: Invalid JSON format in file '{filepath}'", file=sys.stderr
        )
        sys.exit(1)
    except Exception as e:
        print(f"Error loading sentences: {e}", file=sys.stderr)
        sys.exit(1)


def create_fill_in_the_blank(sentence: Dict[str, str]) -> Dict[str, str]:
    """
    Create a fill-in-the-blank question from a sentence.

    Args:
        sentence: Dictionary containing text and translation fields

    Returns:
        Dictionary with question text, answer, and hint
    """
    text = sentence.get("text", "")
    translation = sentence.get("translation", "")

    # Split the text into words, preserving punctuation
    words = re.findall(r"\w+|[^\w\s]", text)

    # Filter out punctuation for selection
    actual_words = [word for word in words if re.match(r"\w+", word)]

    # If no valid words are found, return None
    if not actual_words:
        return None

    # Select a random word to mask
    word_to_mask = random.choice(actual_words)

    # Create masked question text
    question_text = text.replace(word_to_mask, "____", 1)

    return {
        "questionText": question_text,
        "answer": word_to_mask,
        "hint": translation,
    }


def generate_questions(sentences: List[Dict[str, str]]) -> List[Dict[str, str]]:
    """
    Generate fill-in-the-blank questions from a list of sentences.

    Args:
        sentences: List of dictionaries containing text and translation

    Returns:
        List of dictionaries with question text, answer, and hint
    """
    questions = []

    for sentence in sentences:
        question = create_fill_in_the_blank(sentence)
        if question:
            questions.append(question)

    return questions


def main(filepath: str):
    """
    Main function to process sentences and generate fill-in-the-blank questions.

    Args:
        filepath: Path to the JSON file containing sentences
    """
    # Load sentences from the JSON file
    sentences = load_sentences(filepath)

    # Generate questions
    questions = generate_questions(sentences)

    # Write questions to stdout as JSON
    json.dump(questions, sys.stdout, ensure_ascii=False, indent=2)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Generate fill-in-the-blank questions from sentences"
    )
    parser.add_argument(
        "filepath", help="Path to the JSON file containing sentences"
    )
    args = parser.parse_args()

    main(args.filepath)
