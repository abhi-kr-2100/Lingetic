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
  attemptRequest: AttemptRequest,
  getToken: () => Promise<string | null>
): Promise<T> {
  validateAttemptRequestOrDie(attemptRequest);

  const token = await getToken();
  assert(token !== null, "Token was null when attempting question");

  return await fetchOrThrow(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/questions/attempt`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(attemptRequest),
    }
  );
}

export async function fetchQuestions(language: string, getToken: () => Promise<string | null>): Promise<Question[]> {
  assert(language?.trim()?.length > 0, "language is required");

  const token = await getToken();
  assert(token !== null, "Token was null when fetching questions");
  
  const encodedLanguage = encodeURIComponent(language);

  return await fetchOrThrow(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/questions?language=${encodedLanguage}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
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
