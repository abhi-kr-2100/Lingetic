import os
import sys
import argparse
import json
from typing import List, Dict, Any, Optional
from pathlib import Path
from google import genai
from google.api_core import exceptions as google_exceptions  # Import exceptions
from google.genai import types  # Import types for ThinkingConfig
from pydantic import BaseModel
import time  # Import time for sleep
import random  # Import random for jitter
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed

# --- SYSTEM_PROMPT, EXAMPLES, load_schema, Pydantic models remain the same ---
SYSTEM_PROMPT = """You'll be given a theme (politics, introducing yourself, basic words, etc.) and level (Beginner, A1, B2, Advanced, etc.), and you should generate a list of sentences in {language} for a {language} learner to learn from them. You should also generate one translation of each sentence in English.

You should generate as many sentences as asked. Once you've generated a sentence, assume that sentence to be learned. You can then use words used in that sentence to construct more expressive sentences.
"""

EXAMPLES = {
    "French": """
Example:

Theme: Basic verbs, Level: Intermediate, Number: 2

Output:

{
"data": [
    {
        "text": "Je sais !",
        "translations": [
            {
                "text": "I know!"
            }
        ]
    },
    {
        "text": "Tom gagne.",
        "translations": [
            {
                "text": "Tom's winning."
            }
        ]
    }
]
}
""",
    "Turkish": """
Example:

Theme: Basic verbs, Level: Intermediate, Number: 2

Output:

{
"data": [
    {
        "text": "Ben biliyorum!",
        "translations": [
            {
                "text": "I know!"
            }
        ]
    },
    {
        "text": "Tom kazanıyor.",
        "translations": [
            {
                "text": "Tom is winning."
            }
        ]
    }
]
}
""",
    "Swedish": """
Example:

Theme: Basic verbs, Level: Intermediate, Number: 2

Output:

{
"data": [
    {
        "text": "Jag vet!",
        "translations": [
            {
                "text": "I know!"
            }
        ]
    },
    {
        "text": "Tom vinner.",
        "translations": [
            {
                "text": "Tom is winning."
            }
        ]
    }
]
}
""",
    "Japanese": """
Example:

Theme: Basic verbs, Level: Intermediate, Number: 2

Output:

{
"data": [
    {
        "text": "私は知っています！",
        "translations": [
            {
                "text": "I know!"
            }
        ]
    },
    {
        "text": "トムが勝っています。",
        "translations": [
            {
                "text": "Tom is winning."
            }
        ]
    }
]
}
""",
}


def load_schema(schema_path: str) -> List[Dict[str, Any]]:
    """Load and parse the course schema JSON file"""
    try:
        with open(schema_path, "r", encoding="utf-8") as f:
            return json.load(f)["schema"]
    except FileNotFoundError:
        print(f"Error: Schema file '{schema_path}' not found", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(
            f"Error: Invalid JSON format in file '{schema_path}': {e}",
            file=sys.stderr,
        )
        sys.exit(1)
    except Exception as e:
        print(f"Error loading schema: {e}", file=sys.stderr)
        sys.exit(1)


# -- Structure matching Gemini JSON response for course sentences --
class Translation(BaseModel):
    text: str


class Sentence(BaseModel):
    text: str
    translations: List[Translation]


class CourseResult(BaseModel):
    data: List[Sentence]


def make_gemini_api_call(prompt: str, entry: dict) -> dict:
    """
    Gemini API call using correct import and patterns:
    - Initializes genai.Client
    - Calls client.models.generate_content with response_schema (pydantic model)
    - Extracts .parsed if available and returns as dict, otherwise raises error.
    - Implements retry logic for 429 errors.
    """
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print(
            "Error: GEMINI_API_KEY environment variable not set. "
            "Get an API key at https://ai.google.dev/ and run 'export GEMINI_API_KEY=YOUR_KEY'.",
            file=sys.stderr,
        )
        sys.exit(1)

    client = None  # Initialize client outside the loop
    try:
        # Initialize the client (preferred way according to docs)
        client = genai.Client(api_key=api_key)
    except Exception as e:
        print(f"Error initializing Gemini client: {e}", file=sys.stderr)
        sys.exit(1)

    model_name = "gemini-2.5-flash-preview-04-17"

    # Define the generation configuration including the schema
    config = {
        "response_mime_type": "application/json",
        "response_schema": CourseResult,  # Pass the Pydantic model class
        "thinking_config": types.ThinkingConfig(
            thinking_budget=0,
        ),
    }

    max_retries = 5
    base_delay = 5  # Initial delay in seconds for exponential backoff if API doesn't suggest one
    attempt = 0

    while attempt < max_retries:
        try:
            # Make the API call using the client.models.generate_content method
            response = client.models.generate_content(
                model=model_name,  # Specify the model name
                contents=prompt,  # Pass the prompt via 'contents' parameter
                config=config,  # Pass the config dictionary via 'generation_config' parameter
            )

            # Access the parsed Pydantic object directly via response.candidates[0].content.parts[0].function_call
            if hasattr(response, "parsed") and response.parsed is not None:
                # Use model_dump() for Pydantic v2+
                dump = response.parsed.model_dump()
                dump["data"] = [
                    {
                        "text": sentence["text"],
                        "translations": sentence["translations"],
                        "entry": entry,
                    }
                    for sentence in dump["data"]
                ]
                return dump
            else:
                # If response structure is unexpected
                raw_text = getattr(response, "text", "[No text available]")
                raise ValueError(
                    f"Gemini response structure unexpected or empty. "
                    f"Response text: {raw_text}"
                )

        except google_exceptions.ResourceExhausted as err:
            attempt += 1
            if attempt >= max_retries:
                print(
                    f"Error: Reached max retries ({max_retries}) for rate limit error.",
                    file=sys.stderr,
                )
                print(f"Last error: {err}", file=sys.stderr)
                # Instead of exiting, raise the error to be caught by the main loop
                raise err

            # Try to extract suggested delay from error details
            delay = base_delay * (
                2 ** (attempt - 1)
            )  # Default exponential backoff
            try:
                for detail in getattr(err, "details", []):
                    if (
                        detail.get("@type")
                        == "type.googleapis.com/google.rpc.RetryInfo"
                    ):
                        retry_delay_str = detail.get("retryDelay", "")
                        if retry_delay_str.endswith("s"):
                            delay = int(retry_delay_str[:-1])
                            break  # Use the first found retryDelay
            except Exception:
                # Ignore parsing errors, stick to default backoff
                pass

            # Add random jitter (e.g., +/- 1 second) to avoid thundering herd
            jitter = random.uniform(-1, 1)
            actual_delay = max(
                1, delay + jitter
            )  # Ensure delay is at least 1 second

            print(
                f"Rate limit hit (429). Retrying in {actual_delay:.2f} seconds... (Attempt {attempt}/{max_retries})",
                file=sys.stderr,
            )
            time.sleep(actual_delay)

        except Exception as err:
            # Catch other potential API errors or ValueErrors raised above
            # Raise the error so it can be caught and handled in the main loop
            raise err

    # Should not be reached if loop exits normally via return or raise
    # If it does, raise an error indicating max retries were hit without success
    raise google_exceptions.ResourceExhausted(
        f"Max retries ({max_retries}) exceeded without success."
    )


# --- generate_prompt, get_sentences_for_entry, merge_json_data, get_output_file, get_parser remain the same ---


def generate_prompt(
    language: str,
    theme: str,
    level: str,
    number: int,
    instructions: Optional[str],
    example: str,
) -> str:
    """Generate the prompt to send to the Gemini API"""
    system_text = SYSTEM_PROMPT.format(language=language)
    user_part = f"Theme: {theme}, Level: {level}, Number: {number}"
    if instructions:
        user_part += f"\nInstructions: {instructions}"
    prompt = f"{system_text.strip()}\n{example.strip()}\n\n{user_part}\n\nGenerate the sentences now according to the theme, level, and number requested."
    return prompt


def get_sentences_for_entry(language: str, entry: Dict[str, Any]) -> dict:
    """Generate sentences for a single schema entry"""
    lang_key = language
    if lang_key not in EXAMPLES:
        raise ValueError(
            f"Language '{language}' not supported. Supported: {', '.join(EXAMPLES.keys())}"
        )
    example = EXAMPLES[lang_key]
    theme = entry.get("theme", "")
    level = entry.get("level", "")
    number = entry.get("number", 5)  # Default to 5 sentences if not specified
    instructions = entry.get("instructions", "")

    prompt = generate_prompt(
        language, theme, level, number, instructions, example
    )
    # The API call function now returns a dict directly and handles retries/raises errors
    output_dict = make_gemini_api_call(prompt, entry)
    return output_dict


def merge_json_data(data_list: List[dict]) -> dict:
    """Merge multiple API responses into a single dataset"""
    sentences = []
    for d in data_list:
        data_part = d.get("data", [])
        sentences.extend(data_part)
    return {"data": sentences}


def get_output_file(output_path: str) -> Any:
    """Get file handle for output (stdout or file)"""
    if output_path == "-":
        return sys.stdout
    else:
        try:
            Path(output_path).parent.mkdir(parents=True, exist_ok=True)
            return open(output_path, "w", encoding="utf-8")
        except Exception as e:
            print(
                f"Error opening output file '{output_path}': {e}",
                file=sys.stderr,
            )
            sys.exit(1)


def get_parser() -> argparse.ArgumentParser:
    """Configure command line argument parser"""
    parser = argparse.ArgumentParser(
        description="Generate example course sentences using Gemini API and a schema."
    )
    parser.add_argument(
        "-l",
        "--language",
        type=str,
        required=True,
        help="Language (e.g., French, Turkish) (case-sensitive!)",
    )
    parser.add_argument(
        "-s",
        "--schema",
        type=str,
        required=True,
        help="Path to the course schema file (JSON list of objects).",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="-",
        help="Path to the output file (default: '-' for stdout).",
    )
    return parser


def main(language: str, schema: str, output: str):
    """Main function to generate course content"""
    if language not in EXAMPLES:
        print(
            f"Error: Language '{language}' is not supported. Supported: {', '.join(EXAMPLES.keys())}",
            file=sys.stderr,
        )
        sys.exit(1)

    schema_entries = load_schema(schema)

    # Set up logging path for caching
    log_path = (
        Path(output).with_suffix(".log")
        if output != "-"
        else Path("course_output.log")
    )

    # Add sequential IDs to maintain order
    for i, entry in enumerate(schema_entries):
        entry["_seq_id"] = i

    # Load cache from log file
    processed_ids = set()
    log_results = {}
    if log_path.exists():
        with open(log_path, "r", encoding="utf-8") as logf:
            for line in logf:
                try:
                    entry = json.loads(line)
                    if "_seq_id" in entry:
                        processed_ids.add(entry["_seq_id"])
                        log_results[entry["_seq_id"]] = entry
                except Exception as e:
                    print(f"Error reading log line: {e}", file=sys.stderr)

    # Filter entries that need processing
    to_process = [
        (i, entry)
        for i, entry in enumerate(schema_entries)
        if i not in processed_ids
    ]
    total_entries = len(to_process)

    if not to_process:
        print(
            "All entries already processed, using cached results.",
            file=sys.stderr,
        )
        results_map = log_results
    else:
        print(
            f"Generating content for {total_entries} new entries...",
            file=sys.stderr,
        )
        results_map = {i: log_results[i] for i in processed_ids}
        log_lock = threading.Lock()

        def process_entry(idx: int, entry: Dict[str, Any]) -> Dict[str, Any]:
            print(
                f"Processing entry {idx + 1}/{total_entries}: Theme='{entry.get('theme', '')}', Level='{entry.get('level', '')}'",
                file=sys.stderr,
            )
            try:
                # Generate prompt and get sentences
                lang_key = language
                example = EXAMPLES[lang_key]
                theme = entry.get("theme", "")
                level = entry.get("level", "")
                number = entry.get("number", 5)
                instructions = entry.get("instructions", "")

                prompt = generate_prompt(
                    language, theme, level, number, instructions, example
                )
                result = make_gemini_api_call(prompt, entry)
                result["_seq_id"] = entry["_seq_id"]

                # Write to log immediately
                with log_lock:
                    with open(log_path, "a", encoding="utf-8") as logf:
                        logf.write(
                            json.dumps(result, ensure_ascii=False) + "\n"
                        )
                return result
            except Exception as e:
                print(f"Error processing entry {idx + 1}: {e}", file=sys.stderr)
                raise e

        # Process entries asynchronously while maintaining order
        with ThreadPoolExecutor() as executor:
            future_to_idx = {
                executor.submit(process_entry, idx, entry): idx
                for idx, entry in to_process
            }

            for future in as_completed(future_to_idx):
                idx = future_to_idx[future]
                try:
                    result = future.result()
                    results_map[idx] = result
                except Exception as e:
                    print(f"Error processing entry {idx}: {e}", file=sys.stderr)
                    continue

    # Reconstruct results in original order and remove _seq_id
    ordered_results = []
    for i in range(len(schema_entries)):
        if i in results_map:
            result = dict(results_map[i])
            if "_seq_id" in result:
                del result["_seq_id"]
            ordered_results.append(result)

    # Merge and write final output
    if not ordered_results:
        print("\nNo successful results to merge or write.", file=sys.stderr)
    else:
        print("\nMerging successful results...", file=sys.stderr)
        merged = merge_json_data(ordered_results)
        output_file = get_output_file(output)
        needs_closing = output != "-"
        try:
            print(
                f"Writing merged data to {output if output != '-' else 'stdout'}...",
                file=sys.stderr,
            )
            json.dump(merged, output_file, ensure_ascii=False, indent=2)
            output_file.write("\n")
        except Exception as write_err:
            print(f"Error writing output: {write_err}", file=sys.stderr)
        finally:
            if needs_closing:
                output_file.close()

    print("\nDone.", file=sys.stderr)


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.language, args.schema, args.output)
