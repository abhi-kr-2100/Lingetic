import sys
import argparse
import json
import uuid
from typing import List, Dict, Any
from pathlib import Path
from pydantic import BaseModel

from library.gemini_client import get_global_gemini_client


SYSTEM_PROMPT = """You'll be given a theme (politics, introducing yourself, basic words, etc.) and level (Beginner, A1, B2, Advanced, etc.), and you should generate a list of sentences in {language} for a {language} learner to learn from them. You should also generate one translation of each sentence in {translation_language}.

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
        "sourceText": "Je sais !",
        "translationText": "I know!"
    },
    {
        "sourceText": "Tom gagne.",
        "translationText": "Tom's winning."
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
        "sourceText": "Jag vet!",
        "translationText": "I know!"
    },
    {
        "sourceText": "Tom vinner.",
        "translationText": "Tom is winning."
    }
]
}
""",
    "JapaneseModifiedHepburn": """
Example:

Theme: Basic verbs, Level: Intermediate, Number: 2

Output:

{
"data": [
    {
        "sourceText": "Watashi wa shitte imasu!",
        "translationText": "I know!"
    },
    {
        "sourceText": "Tomu ga katteimasu.",
        "translationText": "Tom is winning."
    }
]
}
""",
}


class Sentence(BaseModel):
    sourceText: str
    translationText: str


class CourseResult(BaseModel):
    data: List[Sentence]


def load_schema(schema_path: str) -> List[Dict[str, Any]]:
    """Load and parse the course schema JSON file"""
    with open(schema_path, "r", encoding="utf-8") as f:
        return json.load(f)["schema"]


def make_gemini_api_call(prompt: str) -> dict:
    """
    Make a call to the Gemini API using the GeminiClient.

    Args:
        prompt: The prompt to send to the API
        entry: The entry containing language and other metadata

    Returns:
        dict: The response from the API with entry data added to each sentence
    """
    client = get_global_gemini_client()
    result = client.generate_content(
        prompt=prompt, response_schema=CourseResult
    )

    return result


def generate_prompt(
    language: str,
    translation_language: str,
    entry: Dict[str, Any],
    example: str,
) -> str:
    """Generate the prompt to send to the Gemini API"""
    system_text = SYSTEM_PROMPT.format(
        language=language, translation_language=translation_language
    )
    user_part = f"Theme: {entry['theme']}, Level: {entry['level']}, Number: {entry['number']}"
    if entry.get("instructions"):
        user_part += f"\nInstructions: {entry['instructions']}"
    prompt = f"{system_text.strip()}\n{example.strip()}\n\n{user_part}\n\nGenerate the sentences now according to the theme, level, and number requested."
    return prompt


def merge_json_data(
    data_list: List[dict], language: str, translation_language: str
) -> Dict[str, List[dict]]:
    """Merge multiple API responses into a list of sentences with their translations"""
    result = []
    for d in data_list:
        data_part = d["data"]
        for sentence in data_part:
            sentence_data = {
                "id": str(uuid.uuid4()),
                "sourceText": sentence["sourceText"],
                "translationText": sentence["translationText"],
                "sourceLanguage": language,
                "translationLanguage": translation_language,
            }
            result.append(sentence_data)
    return {"sentences": result}


def get_output_file(output_path: str) -> Any:
    """Get file handle for output (stdout or file)"""
    if output_path == "-":
        return sys.stdout
    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    return open(output_path, "w", encoding="utf-8")


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
        choices=EXAMPLES.keys(),
        help="Course language",
    )
    parser.add_argument(
        "-s",
        "--schema",
        type=str,
        required=True,
        help="Path to the course schema file.",
    )
    parser.add_argument(
        "-t",
        "--translation-language",
        type=str,
        default="English",
        help="Translation language",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="-",
        help="Path to the output file",
    )
    return parser


def main(language: str, schema: str, output: str, translation_language: str):
    """Main function to generate course content"""
    if language not in EXAMPLES:
        raise ValueError(
            f"Language '{language}' is not supported. Supported: {', '.join(EXAMPLES.keys())}"
        )

    schema_entries = load_schema(schema)
    results = []

    for entry in schema_entries:
        example = EXAMPLES[language]

        prompt = generate_prompt(language, translation_language, entry, example)
        result = make_gemini_api_call(prompt)
        results.append(result)

    merged = merge_json_data(results, language, translation_language)
    with get_output_file(output) as f:
        json.dump(merged, f, ensure_ascii=False, indent=2)


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    main(args.language, args.schema, args.output, args.translation_language)
