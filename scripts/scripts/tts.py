#!/usr/bin/env python

from typing import Dict, List, Any
import json
import sys
import argparse
import hashlib
import os
from gtts import gTTS


LANGUAGE_TO_CODE = {
    "English": "en",
    "French": "fr",
    "Turkish": "tr",
}


def generate_filename(text: str, language: str) -> str:
    """
    Generate a unique filename based on the text and language.

    Args:
        text: The text to be converted to speech.
        language: The language of the text.

    Returns:
        A unique filename.
    """
    combined = f"{text.strip()}_{language}".encode("utf-8")
    hash_object = hashlib.sha1(combined)
    return f"{hash_object.hexdigest()}.mp3"


def load_questions(filepath: str) -> List[Dict[str, Any]]:
    """
    Load questions from a JSON file or stdin.

    Args:
        filepath: Path to the JSON file containing questions, or '-' to read from stdin.

    Returns:
        A list of dictionaries with question_type and question_type_specific_data.
    """
    try:
        if filepath == "-":
            return json.load(sys.stdin)
        else:
            with open(filepath, "r", encoding="utf-8") as file:
                return json.load(file).get("data", [])
    except FileNotFoundError:
        print(f"Error: File '{filepath}' not found", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError:
        source = "stdin" if filepath == "-" else f"file '{filepath}'"
        print(f"Error: Invalid JSON format in {source}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"Error loading questions: {e}", file=sys.stderr)
        sys.exit(1)


def text_to_speech(
    questions: List[Dict[str, Any]], language: str, output_dir: str
):
    """
    Convert text to speech for each question and save the audio to the specified directory.

    Args:
        questions: List of questions to be converted.
        language: Language of the questions.
        output_dir: Directory where the audio files will be saved.
    """
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    for question in questions:
        text = question.get("text", "")
        if text:
            filename = generate_filename(text, language)
            file_path = os.path.join(output_dir, filename)

            tts = gTTS(text=text, lang=LANGUAGE_TO_CODE[language])
            tts.save(file_path)

            print(f"Saved: {file_path}")


def main(filepath: str, language: str, output_dir: str):
    """
    Main function to process questions and save them as audio files.

    Args:
        filepath: Path to the JSON file containing questions, or '-' to read from stdin.
        language: Language of the questions.
        output_dir: Directory where the audio files will be saved.
    """
    if language not in LANGUAGE_TO_CODE:
        print(
            f"Error: Language '{language}' not supported.",
            file=sys.stderr,
        )
        sys.exit(1)

    questions = load_questions(filepath)
    text_to_speech(questions, language, output_dir)


def get_parser() -> argparse.ArgumentParser:
    """
    Create and configure the argument parser for the script.

    Returns:
        An ArgumentParser object configured with the script's arguments.
    """
    parser = argparse.ArgumentParser(
        description="Convert questions from a JSON file into speech and save them as MP3 files."
    )
    parser.add_argument(
        "filepath",
        nargs="?",
        default="-",
        help="Path to the JSON file containing questions (default: '-' to read from stdin).",
    )
    parser.add_argument(
        "-l",
        "--language",
        required=True,
        help="Language of the questions (e.g., 'en' for English, 'fr' for French).",
    )
    parser.add_argument(
        "-o",
        "--output-dir",
        required=True,
        help="Directory where the audio files will be saved.",
    )
    return parser


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()

    main(args.filepath, args.language, args.output_dir)
