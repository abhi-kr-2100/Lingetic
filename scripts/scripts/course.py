#!/usr/bin/env python

import os
import sys
import argparse
import json
from typing import List, Dict, Any, Optional
from pathlib import Path
from google import genai
from pydantic import BaseModel

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
}


def load_schema(schema_path: str) -> List[Dict[str, Any]]:
    """Load and parse the course schema JSON file"""
    try:
        with open(schema_path, "r", encoding="utf-8") as f:
            return json.load(f)
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


def make_gemini_api_call(prompt: str) -> dict:
    """
    Gemini API call using correct import and patterns:
    - Initializes genai.Client
    - Calls client.models.generate_content with response_schema (pydantic model)
    - Extracts .parsed if available and returns as dict, otherwise raises error.
    """
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print(
            "Error: GEMINI_API_KEY environment variable not set. "
            "Get an API key at https://ai.google.dev/ and run 'export GEMINI_API_KEY=YOUR_KEY'.",
            file=sys.stderr,
        )
        sys.exit(1)

    try:
        # Initialize the client (preferred way according to docs)
        client = genai.Client(api_key=api_key)

        # Define the model name (e.g., gemini-1.5-flash or the original one)
        # Using 1.5 flash as it's generally recommended and supports JSON mode well
        model_name = "gemini-1.5-flash"
        # model_name = "gemini-2.0-flash-lite" # Or keep the original if preferred

        # Define the generation configuration including the schema
        config = {
            "response_mime_type": "application/json",
            "response_schema": CourseResult,  # Pass the Pydantic model class
        }

        # Make the API call using the client.models.generate_content method
        response = client.models.generate_content(
            model=model_name,  # Specify the model name
            contents=prompt,  # Pass the prompt via 'contents' parameter
            config=config,  # Pass the config dictionary via 'config' parameter
        )

        # Access the parsed Pydantic object directly via response.parsed
        # The documentation indicates .parsed will hold the instantiated object(s)
        if hasattr(response, "parsed") and response.parsed is not None:
            # response.parsed is the CourseResult object. Return its dict representation.
            # Use model_dump() for Pydantic v2+
            return response.parsed.model_dump()
            # For older Pydantic v1, you might use: return response.parsed.dict()
        else:
            # If .parsed is None or missing, the model likely failed to generate
            # valid JSON matching the schema.
            raw_text = getattr(response, "text", "[No text available]")
            raise ValueError(
                f"Gemini did not return the expected structured JSON matching the schema. "
                f"Response text: {raw_text}"
            )

    except Exception as err:
        # Catch potential API errors or ValueErrors raised above
        print(f"Error during Gemini API call: {err}", file=sys.stderr)
        # Optionally, log more details about the response if available
        # print(f"Full response object (if available): {response}", file=sys.stderr)
        sys.exit(1)


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
    # Note: The prompt structure itself doesn't need to change much,
    # as the schema is now passed via config, not just relying on prompt instructions.
    # However, keeping the example helps the model understand the desired *content*.
    user_part = f"Theme: {theme}, Level: {level}, Number: {number}"
    if instructions:
        user_part += f"\nInstructions: {instructions}"
    # The prompt should clearly state the task, the context (theme, level, number),
    # and potentially the desired format (though the schema enforces this).
    # The system prompt already covers the core task.
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
    # The API call function now returns a dict directly
    output_dict = make_gemini_api_call(prompt)
    return output_dict


def merge_json_data(data_list: List[dict]) -> dict:
    """Merge multiple API responses into a single dataset"""
    sentences = []
    for d in data_list:
        # Each item in data_list should now be a dict matching CourseResult structure
        data_part = d.get("data", [])
        sentences.extend(data_part)
    return {"data": sentences}


def get_output_file(output_path: str) -> Any:
    """Get file handle for output (stdout or file)"""
    if output_path == "-":
        return sys.stdout
    else:
        try:
            # Ensure parent directory exists if it's a nested path
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
    results = []
    print(
        f"Generating content for {len(schema_entries)} entries...",
        file=sys.stderr,
    )
    for i, entry in enumerate(schema_entries):
        print(
            f"Processing entry {i + 1}/{len(schema_entries)}: Theme='{entry.get('theme', '')}', Level='{entry.get('level', '')}'",
            file=sys.stderr,
        )
        try:
            res = get_sentences_for_entry(language, entry)
            # Basic validation: check if the result has the expected 'data' key
            if "data" not in res or not isinstance(res["data"], list):
                print(
                    f"Warning: Entry {i + 1} result missing or invalid 'data' key. Result: {res}",
                    file=sys.stderr,
                )
                # Decide whether to skip or add an empty list
                # results.append({"data": []}) # Option: Add empty data
                continue  # Option: Skip this entry
            results.append(res)
            print(f"Entry {i + 1} processed successfully.", file=sys.stderr)
        except Exception as e:
            print(f"Error processing entry {i + 1}: {e}", file=sys.stderr)
            # Decide if one error should stop the whole process
            # sys.exit(1) # Option: Exit on first error
            print(
                f"Skipping entry {i + 1} due to error.", file=sys.stderr
            )  # Option: Continue with others

    print("Merging results...", file=sys.stderr)
    merged = merge_json_data(results)
    output_file = get_output_file(output)
    needs_closing = output != "-"
    try:
        print(
            f"Writing merged data to {output if output != '-' else 'stdout'}...",
            file=sys.stderr,
        )
        json.dump(merged, output_file, ensure_ascii=False, indent=2)
        output_file.write("\n")
        print("Done.", file=sys.stderr)
    finally:
        if needs_closing:
            output_file.close()


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.language, args.schema, args.output)
