#!/usr/bin/env python3
"""
Convert Japanese text to Romaji using the Gemini API.

This script reads a JSON file containing Japanese sentences and converts them to Romaji
using the Hepburn romanization system, while preserving all other fields in the input.
"""

import argparse
import asyncio
import json
import uuid
from typing import Dict, Any, List

from pydantic import BaseModel

from library.gemini_client import get_global_gemini_client

SYSTEM_PROMPT = """Convert Japanese text to Romaji using the Modified Hepburn system with meticulous attention to detail. Adhere strictly to the rules below:

Detailed Romanization Rules:

Rule 1: Long Vowels (Chōon)
All long vowels must be represented with a macron (e.g., ā, ī, ū, ē, ō). Do not use double vowels (e.g., oo, uu) or digraphs (e.g., ou).
ああ → ā (e.g., おかあさん → okāsan)
いい → ī (e.g., おいしい → oishī)
うう → ū (e.g., すうじ → sūji)
ええ → ē (e.g., おねえさん → onēsan)
おお → ō (e.g., おおきい → ōkii)
おう → ō (e.g., ありがとう → arigatō)

The katakana long vowel marker ー also follows this rule. (e.g., ラーメン → rāmen, コーヒー → kōhī)
Exception: For distinct words where the vowels are pronounced separately, do not use a macron. This is rare. (e.g., 思う → omou). When in doubt, default to the macron rule.

Rule 2: Syllabic Nasal ん (Hatsuon)
The romanization of ん is critical and follows strict contextual rules.
    2a. Before Labial Consonants: Before b, p, or m, ん is always romanized as m.
        しんぶん → shimbun
        てんぷら → tempura
        ぐんま → Gumma
    2b. The Mandatory Apostrophe (n'):
        An apostrophe (') MUST be used after n when it precedes a vowel (a, i, u, e, o) or the consonant y. This is not optional and is essential to prevent mispronunciation by separating the n syllable from the following vowel or y-initial syllable.
        Before Vowels: しんい → shin'i (This distinguishes it from しに → shini). かんおん → kan'on (distinguishes from かのん → kanon).
        Before 'y': This is a strict, non-negotiable requirement.
        しんよう → shin'yō (Correct) vs. shinyō (Incorrect, implies しにょう).
        ほんや → hon'ya (Correct) vs. honya (Incorrect, implies ほにゃ).
        じゅんいちろう → Jun'ichirō (Correct).
    2c. Standard 'n':
        In all other contexts (i.e., before any consonant other than b, p, m, y, and at the end of a word), ん is romanized as n.
        かんじ → kanji
        げんき → genki
        あんない → annai

Rule 3: Double Consonants (Sokuon)
The small っ (sokuon) doubles the consonant of the following syllable. (e.g., きって → kitte, がっこう → gakkō, ざっし → zasshi)
Exception: When followed by a ch sound, it becomes tch. (e.g., まっちゃ → matcha)

Rule 4: Particles
Grammatical particles must be romanized based on their pronunciation, not their spelling.
は (as a particle) → wa (e.g., わたしは → watashi wa)
へ (as a particle) → e (e.g., えきへ → eki e)
を (as a particle) → o (e.g., ほんをよむ → hon o yomu)

Rule 5: Spacing & Punctuation
Spacing: Preserve the semantic spacing of the original Japanese. Do not merge distinct words. Romanize おはようございます as Ohayō gozaimasu, not Ohayōgozaimasu.
Punctuation: Convert Japanese punctuation to their standard English equivalents.
。 (maru) → . (period)
、 (ten) → , (comma)
「 and 」 (kagikakko) → " (quotation marks)
？ (tenmaru) → ? (question mark)
！ (tenkuten) → ! (exclamation mark)

Rule 6: Capitalization
Capitalize the first letter of the first word in every sentence. Capitalize all proper nouns, including names of people, places, and organizations. (e.g., 東京 → Tōkyō, 田中さん → Tanaka-san)

Verification Examples:

Example 1 Input: 東京へ行きます。
Example 1 Output: Tōkyō e ikimasu.

Example 2 Input: すみません、ちょっと待ってください。
Example 2 Output: Sumimasen, chotto matte kudasai.

Example 3 Input: これは本や雑誌ではありません。
Example 3 Output: Kore wa hon'ya zasshi dewa arimasen.

Example 4 Input: 「おはようございます」と田中さんは言いました。
Example 4 Output: "Ohayō gozaimasu," to Tanaka-san wa iimashita.

Text:"""


class RomajiResponse(BaseModel):
    """Pydantic model for the Romaji conversion response."""

    romaji: str


async def convert_to_romaji(
    text: str,
) -> str:
    """Convert Japanese text to Romaji using the Gemini API.

    Args:
        text: Japanese text to convert to Romaji

    Returns:
        str: The converted Romaji text
    """
    client = get_global_gemini_client()

    prompt = SYSTEM_PROMPT + f"{text}\n"

    request_id = uuid.uuid5(uuid.NAMESPACE_DNS, f"romaji-{text}")
    response = await client.generate_content(
        prompt=prompt, response_schema=RomajiResponse, request_id=request_id
    )

    return response["romaji"]


def get_parser() -> argparse.ArgumentParser:
    """Configure command line argument parser.

    Returns:
        argparse.ArgumentParser: Configured argument parser
    """
    parser = argparse.ArgumentParser(
        description="Convert Japanese text to Romaji in a JSON file."
    )
    parser.add_argument(
        "input_file", type=str, help="Path to the input JSON file"
    )
    parser.add_argument(
        "-o",
        "--output",
        type=str,
        default="-",
        help="Output file path (default: stdout)",
    )
    return parser


def main(input_file: str, output: str):
    """Main function to handle command line arguments and process the input file."""
    async def async_main():
        with open(input_file, "r", encoding="utf-8") as f:
            data = json.load(f)

        async def process_sentence(sentence: Dict[str, Any]) -> Dict[str, Any]:
            processed = sentence.copy()
            processed["sourceText"] = await convert_to_romaji(processed["sourceText"])
            return processed

        tasks = [process_sentence(sentence) for sentence in data["sentences"]]
        processed_sentences: List[Dict[str, Any]] = await asyncio.gather(*tasks)

        result = {"sentences": processed_sentences}
        output_to_write = json.dumps(result, ensure_ascii=False, indent=2)

        if output == "-":
            print(output_to_write)
        else:
            with open(output, "w", encoding="utf-8") as f:
                f.write(output_to_write)

    asyncio.run(async_main())


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(
        input_file=args.input_file,
        output=args.output,
    )
