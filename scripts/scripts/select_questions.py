#!/usr/bin/env python

import os
import sys
import argparse
import json
import time
import random
from typing import List, Dict, Any, TextIO
from google import genai
from google.api_core import exceptions as google_exceptions
from pydantic import BaseModel

# Pydantic model for response schema
class ChoiceResult(BaseModel):
    choice: int

def make_gemini_api_call(prompt: str) -> int:
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print(
            "Error: GEMINI_API_KEY environment variable not set. "
            "Get an API key at https://ai.google.dev/ and run 'export GEMINI_API_KEY=YOUR_KEY'.",
            file=sys.stderr,
        )
        sys.exit(1)
    try:
        client = genai.Client(api_key=api_key)
    except Exception as e:
        print(f"Error initializing Gemini client: {e}", file=sys.stderr)
        sys.exit(1)

    model_name = "gemini-1.5-flash-8b"

    # Use Pydantic schema to parse response
    config = {
        "response_mime_type": "application/json",
        "response_schema": ChoiceResult,
    }

    max_retries = 5
    base_delay = 5
    attempt = 0

    while attempt < max_retries:
        try:
            response = client.models.generate_content(
                model=model_name,
                contents=prompt,
                config=config,
            )
            if hasattr(response, "parsed") and response.parsed is not None:
                parsed = response.parsed.model_dump()
                return parsed["choice"]
            else:
                raw_text = getattr(response, "text", "")
                raise ValueError(f"Gemini response missing parsed output, got: {raw_text}")

        except google_exceptions.ResourceExhausted as err:
            attempt += 1
            if attempt >= max_retries:
                print(
                    f"Error: Reached max retries ({max_retries}) for rate limit error.",
                    file=sys.stderr,
                )
                print(f"Last error: {err}", file=sys.stderr)
                raise err

            delay = base_delay * (2 ** (attempt - 1))
            jitter = random.uniform(0, base_delay)
            time.sleep(delay + jitter)
        except Exception as e:
            print(f"Error calling Gemini API: {e}", file=sys.stderr)
            sys.exit(1)


def load_entries(filepath: str) -> List[Dict[str, Any]]:
    try:
        if filepath == "-":
            data = json.load(sys.stdin)
        else:
            with open(filepath, "r", encoding="utf-8") as f:
                data = json.load(f)
        return data
    except Exception as e:
        print(f"Error loading entries: {e}", file=sys.stderr)
        sys.exit(1)


def get_output_file(output_path: str) -> TextIO:
    if output_path == "-":
        return sys.stdout
    try:
        return open(output_path, "w", encoding="utf-8")
    except Exception as e:
        print(f"Error opening output file '{output_path}': {e}", file=sys.stderr)
        sys.exit(1)


def generate_prompt(entry: Dict[str, Any], language: str) -> str:
    theme = entry.get("entry", {}).get("theme", "")
    level = entry.get("entry", {}).get("level", "")
    instructions = entry.get("entry", {}).get("instructions", "")
    text = entry.get("text", "")
    questions = entry.get("questions", [])
    variant_lines = []
    for idx, q in enumerate(questions, 1):
        variant_lines.append(f"{idx}. {json.dumps(q, ensure_ascii=False)}")
    variants_str = "\n".join(variant_lines)

    prompt = f'''You are a language-learning assistant. You'll be given a theme, and some instruction, along with several fill-in-the-blank questions. You should choose one of them that is most appropriate for the given theme and instruction.
Theme: {theme}
Instructions: {instructions}
Full Sentence: "{text}"
Fill-in-the-blank question variants:
{variants_str}

For example, if the theme is "People", and the Instructions is "Teach about basic relationships", given:
Theme: People
Instructions: Teach about basic relationships.
Full Sentence: "That man is my father.
Fill-in-the-blank question variants:
1. That ___ is my father.
2. That man is my ___.
3. That man ___ my father.

You should output, {{"choice": 2}} since the second variant is most appropriate for the given theme and instruction.
'''
    return prompt


def process_entries(entries: List[Dict[str, Any]], language: str, use_ai: bool) -> List[Dict[str, Any]]:
    selected_questions: List[Dict[str, Any]] = []
    for entry in entries:
        questions = entry.get("questions", [])
        if not questions:
            continue
        if use_ai:
            prompt = generate_prompt(entry, language)
            choice = make_gemini_api_call(prompt)
            if not 1 <= choice <= len(questions):
                print(f"Error: choice {choice} out of range for entry '{entry.get('text')}'", file=sys.stderr)
                continue
        else:
            # Select the variant with the largest answer length
            lengths = [len(q.get("question_type_specific_data", {}).get("answer", "")) for q in questions]
            max_idx = max(range(len(lengths)), key=lambda i: lengths[i])
            choice = max_idx + 1
        selected_questions.append(questions[choice - 1])
    return selected_questions


def get_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Select best fill-in-the-blank questions using Gemini API."
    )
    parser.add_argument(
        "filepath",
        nargs="?",
        default="-",
        help="Path to the JSON file containing entries (default: '-' to read from stdin)",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="-",
        help="Path to the output file (default: '-' for stdout)",
    )
    parser.add_argument(
        "-l",
        "--language",
        required=True,
        help="Language (case-sensitive!)",
    )
    parser.add_argument(
        "-a", "--use-ai",
        action="store_true",
        help="Use Gemini API for selection (default: pick largest blank locally)",
    )
    return parser


def main(filepath: str, output: str, language: str, use_ai: bool) -> None:
    entries = load_entries(filepath)
    selected = process_entries(entries, language, use_ai)
    output_file = get_output_file(output)
    needs_closing = output != "-"
    try:
        json.dump(selected, output_file, ensure_ascii=False, indent=2)
        output_file.write("\n")
    except Exception as e:
        print(f"Error writing output: {e}", file=sys.stderr)
        sys.exit(1)
    finally:
        if needs_closing:
            output_file.close()


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.filepath, args.output, args.language, args.use_ai)
