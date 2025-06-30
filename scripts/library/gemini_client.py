import os
import json
import asyncio
from pathlib import Path
from typing import Optional, Dict, Any
from uuid import UUID

from google import genai


class GeminiClient:
    """
    An async client for interacting with the Gemini API.
    Handles API key management and provides methods to make API calls.
    """

    def __init__(self, api_key: Optional[str] = None, cache_path: str = ".gemini_cache.json"):
        """
        Initialize the Gemini client.

        Args:
            api_key: Optional API key. If not provided, will try to get from GEMINI_API_KEY environment variable.
            cache_path: Path to the cache file.
        """
        self.api_key = api_key or os.getenv("GEMINI_API_KEY")
        if not self.api_key:
            raise ValueError(
                "No API key provided. Either pass it to the constructor "
                "or set the GEMINI_API_KEY environment variable."
            )

        self.client = genai.Client(api_key=self.api_key)
        self.cache_path = Path(cache_path)
        self.cache_file_lock = asyncio.Lock()
        self.cache_dictionary_lock = asyncio.Lock()
        self.request_locks = {}
        self.request_locks_lock = asyncio.Lock()
        self.cache = self._load_cache()

    def _load_cache(self) -> Dict[str, Any]:
        """Loads the cache from the file."""
        if not self.cache_path.exists():
            return {}
        with open(self.cache_path, "r", encoding="utf-8") as f:
            return json.load(f)

    async def _save_cache(self):
        """Saves the cache to the file."""
        async with self.cache_dictionary_lock:
            cache_to_save = self.cache.copy()
        async with self.cache_file_lock:
            with open(self.cache_path, "w", encoding="utf-8") as f:
                json.dump(cache_to_save, f, ensure_ascii=False, indent=2)

    async def generate_content(self, prompt: str, response_schema, request_id: UUID, **kwargs) -> dict:
        """
        Generate content using the Gemini API asynchronously.

        Args:
            prompt: The prompt to send to the API
            response_schema: The schema to use for parsing the response
            request_id: A unique identifier for the request, used for caching.
            **kwargs: Additional arguments to pass to the API

        Returns:
            The parsed response from the API
        """
        request_id_str = str(request_id)

        async with self.cache_dictionary_lock:
            if request_id_str in self.cache:
                return self.cache[request_id_str]

        # Use a per-request lock to prevent multiple calls for the same request_id,
        # while allowing concurrent calls for different request_ids.
        async with self.request_locks_lock:
            if request_id_str not in self.request_locks:
                self.request_locks[request_id_str] = asyncio.Lock()
            request_lock = self.request_locks[request_id_str]

        async with request_lock:
            # Re-check cache after acquiring the lock in case another thread just populated it.
            async with self.cache_dictionary_lock:
                if request_id_str in self.cache:
                    return self.cache[request_id_str]

            config = {
                "response_mime_type": "application/json",
                "response_schema": response_schema,
                **kwargs,
            }

            response = await self.client.aio.models.generate_content(
                model="gemini-1.5-flash-8b",
                contents=prompt,
                config=config,
            )

            if hasattr(response, "parsed") and response.parsed is not None:
                result = response.parsed.model_dump()
                async with self.cache_dictionary_lock:
                    self.cache[request_id_str] = result
                await self._save_cache()
                return result

            raise ValueError("Gemini response structure unexpected or empty")


global_gemini_client = None


def get_global_gemini_client():
    """Get or create a global Gemini client instance."""
    global global_gemini_client
    if global_gemini_client is None:
        global_gemini_client = GeminiClient()
    return global_gemini_client
