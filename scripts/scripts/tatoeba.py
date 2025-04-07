#!/usr/bin/env python

from typing import Dict, List
import argparse
import json
import sys
import requests


def fetch_sentences(session: requests.Session, url: str) -> Dict:
    """
    Fetch sentences from the Tatoeba API using a session.

    Args:
        session: The requests Session object to use for the request
        url: The API URL to fetch data from

    Returns:
        The JSON response as a dictionary
    """
    response = session.get(url)
    response.raise_for_status()
    return response.json()


def extract_sentences(data: Dict) -> List[Dict]:
    """
    Extract sentences and their translations from the API response.

    Args:
        data: The JSON response from the Tatoeba API

    Returns:
        A list of dictionaries containing the source text and translations
    """
    result = []

    for sentence in data.get("results", []):
        sentence_data = {
            "tatoeba_id": sentence.get("id"),
            "text": sentence.get("text"),
            "lang": sentence.get("lang"),
            "translations": [],
        }

        # Extract translations from all translation groups
        for translation_group in sentence.get("translations", []):
            for translation in translation_group:
                sentence_data["translations"].append(
                    {
                        "tatoeba_id": translation.get("id"),
                        "text": translation.get("text"),
                        "lang": translation.get("lang"),
                    }
                )

        if sentence_data["translations"]:
            result.append(sentence_data)

    return result


def main(
    source_language: str,
    target_language: str = "eng",
    output: str = "-",
):
    """
    Fetch all sentences in the given source language from Tatoeba API.

    Args:
        source_language: The language code to fetch sentences for
        target_language: The language code to filter translations for (default: eng)
        output: Output file path or "-" for stdout (default: "-")
    """
    # Handle file output
    if output == "-":
        output_file = sys.stdout
    else:
        output_file = open(output, "w", encoding="utf-8")

    try:
        base_url = f"https://tatoeba.org/eng/api_v0/search?from={source_language}&to={target_language}&sort=random"
        url = base_url
        all_sentences = []

        # Create a session to reuse the same TCP connection
        with requests.Session() as session:
            # Set common headers for all requests
            session.headers.update(
                {
                    "User-Agent": "TatoebaFetcher/1.0",
                    "Accept": "application/json",
                }
            )

            while url:
                try:
                    data = fetch_sentences(session, url)
                    sentences = extract_sentences(data)
                    all_sentences.extend(sentences)

                    # Check if there are more pages
                    paging = data.get("paging", {}).get("Sentences", {})
                    if paging.get("nextPage", False):
                        next_page = paging.get("page") + 1
                        url = f"{base_url}&page={next_page}"
                        print(
                            f"Fetched {len(all_sentences)} sentences so far...",
                            file=sys.stderr,
                        )
                    else:
                        url = None

                except Exception as e:
                    print(f"Error fetching data: {e}", file=sys.stderr)
                    break

        json.dump(all_sentences, output_file, ensure_ascii=False, indent=2)
    finally:
        if output != "-":
            output_file.close()


def get_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Fetch sentences from Tatoeba API"
    )
    parser.add_argument(
        "source_language", help="Source language code (e.g., eng, deu)"
    )
    parser.add_argument(
        "--target-language",
        "-t",
        default="eng",
        help="Target language code for translations (default: eng)",
    )
    parser.add_argument(
        "--output",
        "-o",
        type=str,
        default="-",
        help="Output file path (default: stdout)",
    )
    return parser


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()

    main(args.source_language, args.target_language, args.output)
