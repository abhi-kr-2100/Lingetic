#!/usr/bin/env python

from typing import Dict, List, TextIO, Any
from string import punctuation
import json
import sys
import random
import argparse


def load_sentences(filepath: str) -> List[Dict[str, Any]]:
    """
    Load sentences from a JSON file or stdin.

    Args:
        filepath: Path to the JSON file containing sentences, or '-' to read from stdin

    Returns:
        A list of dictionaries with text and translation fields
    """
    try:
        if filepath == "-":
            # Read from stdin
            data = json.load(sys.stdin)
        else:
            # Read from file
            with open(filepath, "r", encoding="utf-8") as file:
                data = json.load(file)

        return data.get("data", [])
    except FileNotFoundError:
        print(f"Error: File '{filepath}' not found", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError:
        source = "stdin" if filepath == "-" else f"file '{filepath}'"
        print(f"Error: Invalid JSON format in {source}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"Error loading sentences: {e}", file=sys.stderr)
        sys.exit(1)


def create_fill_in_the_blank(sentence: Dict[str, Any]) -> Dict[str, Any]:
    """
    Create a fill-in-the-blank question from a sentence.

    Args:
        sentence: Dictionary containing text and translation fields

    Returns:
        Dictionary with question type and question-specific data
    """
    text = sentence.get("text", "")
    translation = sentence.get("translations", [])[0].get("text", "")

    split_words = text.split()
    actual_words = []

    for word in split_words:
        clean_word = word.strip(punctuation)
        if not clean_word:
            continue

        actual_words.append(clean_word)

    word_to_mask = random.choice(actual_words)
    question_text = text.replace(word_to_mask, "_____", 1)

    return {
        "question_type": "FillInTheBlanks",
        "question_type_specific_data": {
            "questionText": question_text,
            "answer": word_to_mask,
            "hint": translation,
        },
    }


def generate_questions(sentences: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """
    Generate fill-in-the-blank questions from a list of sentences.

    Args:
        sentences: List of dictionaries containing text and translation

    Returns:
        List of dictionaries with question type and question-specific data
    """
    questions = []

    for sentence in sentences:
        question = create_fill_in_the_blank(sentence)
        if question:
            questions.append(question)

    return questions


def get_output_file(output_path: str) -> TextIO:
    """
    Get the output file handle based on the output path.

    Args:
        output_path: Path to the output file, or '-' for stdout

    Returns:
        A file-like object for writing output
    """
    if output_path == "-":
        return sys.stdout
    else:
        try:
            return open(output_path, "w", encoding="utf-8")
        except Exception as e:
            print(
                f"Error opening output file '{output_path}': {e}",
                file=sys.stderr,
            )
            sys.exit(1)


def main(filepath: str, output: str) -> None:
    """
    Main function to process sentences and generate fill-in-the-blank questions.

    Args:
        filepath: Path to the JSON file containing sentences, or '-' to read from stdin
        output_path: Path to the output file, or '-' for stdout
    """
    # Load sentences from the JSON file or stdin
    sentences = load_sentences(filepath)

    # Generate questions
    questions = generate_questions(sentences)

    # Get output file handle
    output_file = get_output_file(output)

    # Only need to handle closing for actual files (not stdout)
    needs_closing = output != "-"

    try:
        # Write questions as JSON to the output file
        json.dump(questions, output_file, ensure_ascii=False, indent=2)

        # Add a newline at the end of the output
        output_file.write("\n")
    except Exception as e:
        print(f"Error writing to output: {e}", file=sys.stderr)
        sys.exit(1)
    finally:
        # Close the output file if it's not stdout
        if needs_closing:
            output_file.close()


def get_parser() -> argparse.ArgumentParser:
    """
    Create and configure the argument parser for the script.

    Returns:
        An ArgumentParser object configured with the script's arguments
    """
    parser = argparse.ArgumentParser(
        description="Generate fill-in-the-blank questions from sentences"
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


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()

    main(args.filepath, args.output)
