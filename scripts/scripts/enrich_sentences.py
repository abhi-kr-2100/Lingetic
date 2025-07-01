import argparse
import json
import sys
import uuid
from pathlib import Path
from typing import Any

from pydantic import BaseModel

from library.gemini_client import get_global_gemini_client


class DifficultyRating(BaseModel):
    difficulty: int


def get_parser() -> argparse.ArgumentParser:
    """Configure command line argument parser."""
    parser = argparse.ArgumentParser(
        description="Enrich sentence data with IDs, languages, and difficulty ratings."
    )
    parser.add_argument(
        "input",
        nargs="?",
        default="-",
        help="Path to the input file. Defaults to stdin.",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="-",
        help="Path to the output file. Defaults to stdout.",
    )
    parser.add_argument(
        "-s",
        "--source-language",
        required=True,
        help="Source language of the sentences.",
    )
    parser.add_argument(
        "-t",
        "--translation-language",
        default="English",
        help="Translation language of the sentences.",
    )
    return parser


def get_content(input_path: str) -> str:
    """Reads content from a file or stdin."""
    if input_path == "-":
        return sys.stdin.read().strip()
    with open(input_path, "r", encoding="utf-8") as f:
        return f.read().strip()


def get_output_file(output_path: str) -> Any:
    """Get file handle for output (stdout or file)"""
    if output_path == "-":
        return sys.stdout
    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    return open(output_path, "w", encoding="utf-8")


def get_prompt_template() -> str:
    """Reads the prompt template from the file."""
    with open("../prompts/rate_sentence_difficulty.md", "r", encoding="utf-8") as f:
        return f.read()


def get_difficulty_from_llm(sentence: str, prompt_template: str, request_id: uuid.UUID) -> int:
    """Gets the difficulty of a sentence from the LLM."""
    client = get_global_gemini_client()
    prompt = prompt_template + f"\n\nSentence: \"{sentence}\""
    response = client.generate_content(prompt, response_schema=DifficultyRating, request_id=request_id)
    return response['difficulty']


def main(input: str, output: str, source_language: str, translation_language: str):
    """Main function to enrich sentence data."""
    prompt_template = get_prompt_template()
    content = get_content(input)
    data = json.loads(content)
    all_entries = data["sentences"]

    enriched_sentences = []
    for sentence in all_entries:
        request_id = uuid.uuid5(uuid.NAMESPACE_DNS, f'enrich-sentence-{sentence["sourceText"]}-{source_language}')
        difficulty = get_difficulty_from_llm(sentence["sourceText"], prompt_template, request_id)
        enriched_sentences.append(
            {
                **sentence,
                "llm_difficulty": difficulty,
            }
        )

    enriched_sentences.sort(key=lambda x: (x["llm_difficulty"], len(x["sourceText"])))

    final_sentences = []
    for i, sentence in enumerate(enriched_sentences):
        final_sentences.append(
            {
                "id": str(uuid.uuid4()),
                "sourceText": sentence["sourceText"],
                "translationText": sentence["translationText"],
                "sourceLanguage": source_language,
                "translationLanguage": translation_language,
                "difficulty": i * 10,
            }
        )

    with get_output_file(output) as f:
        json.dump({"sentences": final_sentences}, f, ensure_ascii=False, indent=2)


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.input, args.output, args.source_language, args.translation_language)
