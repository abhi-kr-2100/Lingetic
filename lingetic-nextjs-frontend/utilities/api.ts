import type { AttemptResponse, Question, AttemptRequest } from "./api-types";

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
  const encodedLanguage = encodeURIComponent(language);

  return await fetchOrThrow(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/questions?language=${encodedLanguage}`
  );
}
