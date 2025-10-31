import argparse
import json
import uuid
from pathlib import Path
import asyncio
import logging
import sys
from typing import List
from textwrap import dedent

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
    parser = argparse.ArgumentParser(description="Extract sentences from a PDF textbook")
    parser.add_argument(
        "--book-path",
        required=True,
        type=Path,
        help="The path to the book PDF file",
    )
    parser.add_argument(
        "--output-path",
        required=True,
        type=Path,
        help="The path to the output JSON file",
    )
    return parser


class Sentence(BaseModel):
    text: str


class SentenceList(BaseModel):
    sentences: List[Sentence]


async def process_page(page_number: int, page_text: str, semaphore: asyncio.Semaphore) -> List[Sentence]:
    async with semaphore:
        gemini_client = get_global_gemini_client()
        prompt = dedent(f"""
        You are an expert in language learning.
        You will be given a page from a textbook for learning a language.
        Your task is to extract the exemplary sentences from the textbook page.
        The output should be a JSON object with a single key, "sentences", which is a list of objects, each with a single key, "text", which is the sentence.
        Do not choose sentences that are part of a dialog.
        Do not choose sentences that are part of a list of sentences.
        Do not choose sentences that are part of a table.
        Choose sentences that are used to explain a grammatical concept.
        For example, if the textbook says, "The past tense is formed by adding -ed to the verb. For example, 'He walked to the store.'", you should choose the sentence "He walked to the store.".

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
                response_schema=SentenceList.model_json_schema(),
                request_id=request_id,
            )
            # Assuming the response is a dict that can be unpacked into the Pydantic model
            return SentenceList(**response).sentences
        except Exception as e:
            logger.warning("Skipping page %d due to error: %s", page_number, e)
            return []

async def main(book_path: Path, output_path: Path):
    if not book_path.exists():
        raise FileNotFoundError(f"Book file not found at {book_path}")

    reader = PdfReader(book_path)

    semaphore = asyncio.Semaphore(5) # Limit to 5 concurrent requests
    tasks = []
    for i, page in enumerate(reader.pages):
        page_text = page.extract_text()
        if page_text:
            tasks.append(process_page(i + 1, page_text, semaphore))

    pages_sentences = await asyncio.gather(*tasks)

    all_sentences = [sentence for page_sentences in pages_sentences for sentence in page_sentences]

    final_output = SentenceList(sentences=all_sentences)

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(final_output.model_dump(), f, ensure_ascii=False, indent=2)

if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    asyncio.run(main(args.book_path, args.output_path))
