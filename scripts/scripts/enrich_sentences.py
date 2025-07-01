import argparse
import asyncio
import json
import sys
import uuid
from pathlib import Path
from typing import Any, Coroutine, List, Dict

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


async def get_difficulty_from_llm(
    sentence: str, prompt_template: str, request_id: uuid.UUID
) -> int:
    """Gets the difficulty of a sentence from the LLM."""
    client = get_global_gemini_client()
    prompt = prompt_template + f"\n\nSentence: \"{sentence}\""
    response = await client.generate_content(
        prompt, response_schema=DifficultyRating, request_id=request_id
    )
    return response["difficulty"]


async def enrich_sentence(
    sentence: Dict[str, Any], source_language: str, prompt_template: str
) -> Dict[str, Any]:
    request_id = uuid.uuid5(
        uuid.NAMESPACE_DNS,
        f'enrich-sentence-{sentence["sourceText"]}-{source_language}',
    )
    difficulty = await get_difficulty_from_llm(
        sentence["sourceText"], prompt_template, request_id
    )
    return {
        **sentence,
        "llm_difficulty": difficulty,
    }


async def async_main(
    input_path: str, output_path: str, source_language: str, translation_language: str
):
    """Asynchronous main function to enrich sentence data."""
    prompt_template = get_prompt_template()
    content = get_content(input_path)
    data = json.loads(content)
    all_entries = data["sentences"]

    tasks: List[Coroutine] = [
        enrich_sentence(sentence, source_language, prompt_template)
        for sentence in all_entries
    ]
    enriched_sentences: List[Dict[str, Any]] = await asyncio.gather(*tasks)

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

    with get_output_file(output_path) as f:
        json.dump({"sentences": final_sentences}, f, ensure_ascii=False, indent=2)


def main(
    input: str, output: str, source_language: str, translation_language: str
):
    """Main function to enrich sentence data."""
    asyncio.run(async_main(input, output, source_language, translation_language))


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.input, args.output, args.source_language, args.translation_language)

