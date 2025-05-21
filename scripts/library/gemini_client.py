import os
from google import genai
from typing import Optional


class GeminiClient:
    """
    A client for interacting with the Gemini API.
    Handles API key management and provides methods to make API calls.
    """

    def __init__(self, api_key: Optional[str] = None):
        """
        Initialize the Gemini client.

        Args:
            api_key: Optional API key. If not provided, will try to get from GEMINI_API_KEY environment variable.
        """
        self.api_key = api_key or os.getenv("GEMINI_API_KEY")
        if not self.api_key:
            raise ValueError(
                "No API key provided. Either pass it to the constructor "
                "or set the GEMINI_API_KEY environment variable."
            )

        self.client = genai.Client(api_key=self.api_key)

    def generate_content(self, prompt: str, response_schema, **kwargs) -> dict:
        """
        Generate content using the Gemini API.

        Args:
            prompt: The prompt to send to the API
            response_schema: The schema to use for parsing the response
            **kwargs: Additional arguments to pass to the API

        Returns:
            The parsed response from the API
        """
        config = {
            "response_mime_type": "application/json",
            "response_schema": response_schema,
            "thinking_config": genai.types.ThinkingConfig(thinking_budget=0),
            **kwargs,
        }

        response = self.client.models.generate_content(
            model="gemini-2.5-flash-preview-04-17",
            contents=prompt,
            config=config,
        )

        if hasattr(response, "parsed") and response.parsed is not None:
            return response.parsed.model_dump()

        raise ValueError("Gemini response structure unexpected or empty")


global_gemini_client = None


def get_global_gemini_client():
    """Get or create a global Gemini client instance."""
    global global_gemini_client
    if global_gemini_client is None:
        global_gemini_client = GeminiClient()
    return global_gemini_client
