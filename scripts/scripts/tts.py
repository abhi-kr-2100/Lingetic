#!/usr/bin/env python

from typing import Dict, List, Any
import json
import sys
import argparse
import os
from gtts import gTTS
from pydub import AudioSegment


LANGUAGE_TO_CODE = {
    "English": "en",
    "French": "fr",
    "Turkish": "tr",
    "Swedish": "sv",
    "JapaneseModifiedHepburn": "ja",
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


def generate_filename(sentence_id: str) -> str:
    """
    Generate a filename based on sentence id.

    Args:
        sentence_id: The unique id of the sentence.

    Returns:
        The MP3 filename (id.mp3).
    """
    return f"{sentence_id}.mp3"


def load_sentences(filepath: str) -> List[Dict[str, Any]]:
    """
    Load sentences from a JSON file or stdin.

    Args:
        filepath: Path to the JSON file containing sentences, or '-' to read from stdin.

    Returns:
        A list of dictionaries with id, sourceText, translationText, sourceLanguage, and translationLanguage.
    """
    if filepath == "-":
        data = json.load(sys.stdin)
    else:
        with open(filepath, "r", encoding="utf-8") as file:
            data = json.load(file)
    return data["sentences"]


def text_to_speech(
    sentences: List[Dict[str, Any]],
    language: str,
    output_dir: str,
    mode: str = "slow",
):
    """
    Convert text to speech for each sentence and save the audio to the specified directory.

    Args:
        sentences: List of sentences to be converted.
        language: Language of the sentences.
        output_dir: Directory where the audio files will be saved.
        mode: "slow" or "normal" (default is "slow")
    """
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    slow = True if mode == "slow" else False

    for sentence in sentences:
        sentence_id = sentence.get("id")
        text = sentence.get("sourceText")

        filename = generate_filename(sentence_id)
        file_path = os.path.join(output_dir, filename)

        if os.path.exists(file_path):
            continue

        tts = gTTS(text=text, lang=LANGUAGE_TO_CODE[language], slow=slow)
        tts.save(file_path)
        compress_mp3(file_path)


def main(filepath: str, language: str, output_dir: str, mode: str):
    """
    Main function to process sentences and save them as audio files.

    Args:
        filepath: Path to the JSON file containing sentences, or '-' to read from stdin.
        language: Language of the sentences.
        output_dir: Directory where the audio files will be saved.
        mode: "slow" or "normal"
    """
    if language not in LANGUAGE_TO_CODE:
        raise ValueError(f"Language '{language}' not supported.")

    if mode not in ("slow", "normal"):
        raise ValueError("--mode must be either 'slow' or 'normal'")

    sentences = load_sentences(filepath)
    text_to_speech(sentences, language, output_dir, mode)


def get_parser() -> argparse.ArgumentParser:
    """
    Create and configure the argument parser for the script.

    Returns:
        An ArgumentParser object configured with the script's arguments.
    """
    parser = argparse.ArgumentParser(
        description="Convert sentences from a JSON file into speech and save them as MP3 files."
    )
    parser.add_argument(
        "filepath",
        nargs="?",
        default="-",
        help="Path to the JSON file containing sentences (default: '-' to read from stdin).",
    )
    parser.add_argument(
        "-l",
        "--language",
        required=True,
        help="Language of the sentences.",
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
