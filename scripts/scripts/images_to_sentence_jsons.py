import os
import sys
import asyncio
import aiofiles
import base64
from google import genai
from google.genai import Client, types
from tenacity import (
    retry,
    stop_after_attempt,
    wait_exponential,
    retry_if_exception_type,
)
from argparse import ArgumentParser

SUPPORTED_EXTENSIONS = [".png", ".jpg", ".jpeg"]
CONCURRENCY_LIMIT = 5
MODEL = "gemini-2.5-pro-exp-03-25"

# Retry configuration
MAX_RETRIES = 5
MIN_WAIT_SECONDS = 1
MAX_WAIT_SECONDS = 60

PROMPT = """
You'll be given an excerpt from a {language} book that contains sentences in {language} and English. You should choose complete sentences, and in the text field, write the original {language} text, and in the translation field, write the English text.

Your transcriptions will be used to help others learn {language}. So, you should keep your sentences simple; for example, the complete dialog: "Hello! Good morning. I'm Emilia. How are you?" can be broken down into separate sentences, "Hello!", "Good morning.", "I'm Emilia.", "How are you?"

Moreoever, you should verify and correct the English translations if they are incomplete or misleading.

Return your output in the following JSON format:
"""
JSON_SCHEMA = """
{
  "data": [
    {
        "text": "string",
        "translation": "string"
    }
  ]
}
"""


def get_api_key():
    """Return API key required to make calls to the LLM service."""
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        print("Error: GEMINI_API_KEY environment variable is not set.")
        print(
            "Please set your API key with: export GEMINI_API_KEY='your-api-key'"
        )
        sys.exit(1)
    return api_key


@retry(
    stop=stop_after_attempt(MAX_RETRIES),
    wait=wait_exponential(
        multiplier=1, min=MIN_WAIT_SECONDS, max=MAX_WAIT_SECONDS
    ),
    retry=retry_if_exception_type(
        (asyncio.TimeoutError, ConnectionError, Exception)
    ),
    reraise=True,
)
async def call_ai_with_retry(client: Client, image_data, language):
    """Make API call to the Gemini model with retry logic."""
    prompt = PROMPT.format(language=language) + "\n" + JSON_SCHEMA
    response = client.models.generate_content(
        model=MODEL,
        contents=[
            types.Part.from_text(text=prompt),
            types.Part.from_bytes(
                data=base64.b64decode(image_data), mime_type="image/jpeg"
            ),
        ],
    )
    return response.candidates[0].content.parts[0]


async def process_image(client, image_path, output_dir, language):
    """Process a single image and save the extracted text as JSON."""
    image_name = os.path.basename(image_path)
    filename = os.path.splitext(os.path.basename(image_path))[0] + ".json"
    output_file = os.path.join(output_dir, filename)

    if os.path.exists(output_file):
        print(f"⏭ Skipping {image_name} (JSON already exists)")
        return

    try:
        with open(image_path, "rb") as f:
            image_data = base64.b64encode(f.read()).decode("utf-8")

        try:
            print(f"🔄 Processing {image_name}...")
            response = await call_ai_with_retry(client, image_data, language)

            print(f"\n--- Raw response for {image_name} ---")
            print(repr(response))
            print("-----------------------------------\n")

            # Extract the JSON content from response
            result = response.text

            async with aiofiles.open(output_file, "w") as f:
                await f.write(result)
            print(f"✓ Processed {image_name}")

        except Exception as e:
            print(
                f"✗ Failed to process {image_name} after {MAX_RETRIES} attempts: {str(e)}"
            )
            import traceback

            print(f"Error details: {traceback.format_exc()}")

    except Exception as e:
        print(f"✗ Error reading {image_name}: {str(e)}")


async def process_directory(image_dir, language):
    """Process all images in the directory with concurrency control."""
    output_dir = os.path.join(image_dir, "json_output")
    os.makedirs(output_dir, exist_ok=True)

    # Initialize Gemini
    api_key = get_api_key()
    client = genai.Client(api_key=api_key)

    # Get list of image files
    image_files = [
        os.path.join(image_dir, filename)
        for filename in os.listdir(image_dir)
        if any(filename.lower().endswith(ext) for ext in SUPPORTED_EXTENSIONS)
    ]

    if not image_files:
        print(f"No supported images found in {image_dir}")
        return

    # Count how many images already have JSON files
    existing_json_count = 0
    for image_path in image_files:
        json_filename = (
            os.path.splitext(os.path.basename(image_path))[0] + ".json"
        )
        json_path = os.path.join(output_dir, json_filename)
        if os.path.exists(json_path):
            existing_json_count += 1

    print(
        f"Found {len(image_files)} images to process ({existing_json_count} already processed)"
    )

    # Process images with concurrency limit
    semaphore = asyncio.Semaphore(CONCURRENCY_LIMIT)

    async def process_with_semaphore(image_path):
        async with semaphore:
            await process_image(client, image_path, output_dir, language)
            await asyncio.sleep(1)

    await asyncio.gather(*[process_with_semaphore(img) for img in image_files])
    print(f"All processing complete. Results saved to {output_dir}")


def main(image_directory: str, language: str) -> None:
    """
    Process images in a directory and extract sentences using AI.

    Args:
        image_directory: Path to directory containing images
        language: Source language to extract from images
    """
    if not os.path.isdir(image_directory):
        print(
            f"Error: {image_directory} is not a valid directory",
            file=sys.stderr,
        )
        sys.exit(1)

    print("Using Google's Gemini API")
    asyncio.run(process_directory(image_directory, language))


if __name__ == "__main__":
    parser = ArgumentParser(
        description="Extract sentences from images using AI"
    )
    parser.add_argument(
        "image_directory", help="Directory containing images to process"
    )
    parser.add_argument(
        "language",
        help="Source language to extract from images (e.g., Turkish, Spanish)",
    )
    args = parser.parse_args()
    main(args.image_directory, args.language)
