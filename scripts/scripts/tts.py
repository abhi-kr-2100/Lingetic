#!/usr/bin/env python

from typing import Dict, List, Any
import json
import re
import sys
import argparse
import os
from gtts import gTTS
from pydub import AudioSegment


LANGUAGE_TO_CODE = {
    "English": "en",
    "French": "fr",
    "Turkish": "tr",
}


def compress_mp3(
    file_path: str, target_bitrate: str = "32k", sample_rate: int = 16000
):
    """
    Re-encode an MP3 to reduce size by lowering bitrate and sample rate.

    Args:
        file_path: Path to the MP3 file to compress.
        target_bitrate: Desired audio bitrate (e.g., '48k').
        sample_rate: Target sample rate in Hz (e.g., 22050).
    """
    audio = AudioSegment.from_mp3(file_path)
    audio = audio.set_channels(1).set_frame_rate(sample_rate)
    audio.export(file_path, format="mp3", bitrate=target_bitrate)


def generate_filename(question_id: str) -> str:
    """
    Generate a filename based on question id.

    Args:
        question_id: The unique id of the question.

    Returns:
        The MP3 filename (id.mp3).
    """
    return f"{question_id}.mp3"


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
                data = json.load(file)
                return data if isinstance(data, list) else data.get("data", [])
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
    questions: List[Dict[str, Any]],
    language: str,
    output_dir: str,
    mode: str = "slow",
):
    """
    Convert text to speech for each question and save the audio to the specified directory.

    Args:
        questions: List of questions to be converted.
        language: Language of the questions.
        output_dir: Directory where the audio files will be saved.
        mode: "slow" or "normal" (default is "slow")
    """
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    slow = True if mode == "slow" else False

    for question in questions:
        qid = question.get("id")
        qdata = question.get("question_type_specific_data", {})
        text = qdata.get("questionText", "")
        answer = qdata.get("answer", "")
        if text and answer:
            filename = generate_filename(qid)
            file_path = os.path.join(output_dir, filename)

            if os.path.exists(file_path):
                print(f"File {file_path} already exists. Skipping.")
                continue

            full_text = re.sub(r"_+", answer, text)

            tts = gTTS(
                text=full_text, lang=LANGUAGE_TO_CODE[language], slow=slow
            )
            tts.save(file_path)
            compress_mp3(file_path)

            print(f"Saved: {file_path}")


def main(filepath: str, language: str, output_dir: str, mode: str):
    """
    Main function to process questions and save them as audio files.

    Args:
        filepath: Path to the JSON file containing questions, or '-' to read from stdin.
        language: Language of the questions.
        output_dir: Directory where the audio files will be saved.
        mode: "slow" or "normal"
    """
    if language not in LANGUAGE_TO_CODE:
        print(
            f"Error: Language '{language}' not supported.",
            file=sys.stderr,
        )
        sys.exit(1)

    if mode not in ("slow", "normal"):
        print(
            "Error: --mode must be either 'slow' or 'normal'", file=sys.stderr
        )
        sys.exit(1)

    questions = load_questions(filepath)
    text_to_speech(questions, language, output_dir, mode)


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
        help="Language of the questions.",
    )
    parser.add_argument(
        "-o",
        "--output-dir",
        required=True,
        help="Directory where the audio files will be saved.",
    )
    parser.add_argument(
        "-m",
        "--mode",
        choices=["slow", "normal"],
        default="slow",
        help="Set speech mode: 'slow' (default) or 'normal' speed.",
    )
    return parser


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()

    main(args.filepath, args.language, args.output_dir, args.mode)
