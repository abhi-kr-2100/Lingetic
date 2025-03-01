#!/usr/bin/env python

from typing import Dict, List

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

        result.append(sentence_data)

    return result


def main(source_language: str):
    """
    Fetch all sentences in the given source language from Tatoeba API.

    Args:
        source_language: The language code to fetch sentences for
    """
    base_url = f"https://tatoeba.org/eng/api_v0/search?from={source_language}&sort=random"
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

    json.dump(all_sentences, sys.stdout, ensure_ascii=False, indent=2)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(
            "Usage: python tatoeba.py <source_language>",
            file=sys.stderr,
        )
        sys.exit(1)

    main(sys.argv[1])
