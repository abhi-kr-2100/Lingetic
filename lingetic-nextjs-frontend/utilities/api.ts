import type { AttemptResponse, Question, AttemptRequest } from "./api-types";
import assert from "./assert";

async function fetchOrThrow<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, options);
  if (response.ok) {
    return response.json();
  }
  throw new Error(response.statusText);
}

export async function attemptQuestion<T extends AttemptResponse>(
  attemptRequest: AttemptRequest
): Promise<T> {
  validateAttemptRequestOrDie(attemptRequest);

  return await fetchOrThrow(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/questions/attempt`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(attemptRequest),
    }
  );
}

export async function fetchQuestions(language: string): Promise<Question[]> {
  assert(language?.trim()?.length > 0, "language is required");

  const encodedLanguage = encodeURIComponent(language);

  return await fetchOrThrow(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/questions?language=${encodedLanguage}`
  );
}

function validateAttemptRequestOrDie(
  attemptRequest: any
): asserts attemptRequest is AttemptRequest {
  assert(
    attemptRequest?.questionID?.trim()?.length > 0,
    "questionID is nullish or blank"
  );
  assert(
    attemptRequest?.questionType?.trim()?.length > 0,
    "questionType is nullish or blank"
  );
}
