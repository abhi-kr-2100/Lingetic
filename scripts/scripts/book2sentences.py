import argparse
import asyncio
import json
import logging
import os
import sys
import uuid
from pathlib import Path
from textwrap import dedent
from typing import List

from pydantic import BaseModel
from pypdf import PdfReader

from library.gemini_client import get_global_gemini_client

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler(sys.stderr)],
)
logger = logging.getLogger(__name__)


def get_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Extract sentences from a PDF textbook"
    )
    parser.add_argument(
        "filepath",
        nargs=1,
        help="The path to the book PDF file",
    )
    parser.add_argument(
        "-l",
        "--language",
        required=True,
        help="Language of the sentences.",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="-",
        help="The path to the output JSON file",
    )
    return parser


class Sentence(BaseModel):
    text: str


class SentenceList(BaseModel):
    sentences: List[Sentence]


def extract_text_from_pdf(filepath: str) -> List[str]:
    """Extracts text from each page of a PDF file."""
    if not os.path.exists(filepath):
        raise FileNotFoundError(f"Book file not found at {filepath}")

    reader = PdfReader(filepath)
    page_texts = []
    for page in reader.pages:
        page_text = page.extract_text()
        if page_text:
            page_texts.append(page_text)
    return page_texts


def write_sentences(sentences: SentenceList, output_path: str):
    """Writes the sentences to a JSON file or stdout."""
    model_dump = sentences.model_dump()
    if output_path == "-":
        print(json.dumps(model_dump, ensure_ascii=False, indent=2))
    else:
        output_p = Path(output_path)
        output_p.parent.mkdir(parents=True, exist_ok=True)
        with open(output_p, "w", encoding="utf-8") as f:
            json.dump(model_dump, f, ensure_ascii=False, indent=2)


async def process_page(
    language: str,
    page_number: int,
    page_text: str,
    semaphore: asyncio.Semaphore,
) -> List[Sentence]:
    async with semaphore:
        gemini_client = get_global_gemini_client()
        prompt = dedent(f"""
        You are an expert in language learning.
        You will be given a page from a textbook for learning {language}.
        Your task is to extract the exemplary sentences from the textbook page.
        The output should be a JSON object with a single key, "sentences", which is a list of objects, each with a single key, "text", which is the sentence in {language}.

        Ensure that the sentence is complete and grammatically correct.

        Textbook page content:
        {page_text}
        """)

        try:
            request_id = uuid.uuid5(
                uuid.NAMESPACE_DNS,
                f"book2sentences-{page_text}",
            )
            response = await gemini_client.generate_content(
                prompt=prompt,
                response_schema=SentenceList,
                request_id=request_id,
            )
            return response["sentences"]
        except Exception as e:
            logger.warning("Skipping page %d due to error: %s", page_number, e)
            return []


async def async_main(filepath: str, output: str, language: str):
    page_texts = extract_text_from_pdf(filepath)

    semaphore = asyncio.Semaphore(5)
    tasks = [
        process_page(language, i + 1, page_text, semaphore)
        for i, page_text in enumerate(page_texts)
    ]

    pages_sentences = await asyncio.gather(*tasks)

    all_sentences = [
        sentence
        for page_sentences in pages_sentences
        for sentence in page_sentences
    ]

    final_output = SentenceList(sentences=all_sentences)
    write_sentences(final_output, output)


def main(filepath: str, output: str, language: str):
    asyncio.run(async_main(filepath, output, language))


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.filepath, args.output, args.language)
