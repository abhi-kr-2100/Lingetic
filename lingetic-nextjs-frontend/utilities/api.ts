async function fetchOrThrow<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, options);
  if (response.ok) {
    return response.json();
  }
  throw new Error(response.statusText);
}

export type QuestionType = "FillInTheBlanks";
export type AttemptStatus = "Success" | "Failure";

export async function attemptQuestion(
  questionID: string,
  type: QuestionType,
  userResponse: string
): Promise<AttemptResponse> {
  return await fetchOrThrow(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/questions/attempt`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ questionID, type, userResponse }),
    }
  );
}

export async function fetchQuestions(language: string): Promise<Question[]> {
  const encodedLanguage = encodeURIComponent(language);

  return await fetchOrThrow(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/questions?language=${encodedLanguage}`
  );
}

export type AttemptResponse = {
  attemptStatus: AttemptStatus;
  comment?: string;
  answer?: string;
};

export interface Question {
  id: string;
  type: "FillInTheBlanks";
  text: string;
  hint: string;
}
