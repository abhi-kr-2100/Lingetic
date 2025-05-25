import type {
  AttemptResponse,
  QuestionDTO,
  AttemptRequest,
  QuestionType,
  AssetType,
} from "./api-types";
import assert from "./assert";
import { shuffleInPlace } from "./helpers";

async function fetchOrThrow<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, options);
  if (response.ok) {
    return response.json();
  }
  throw new Error(response.statusText);
}

export async function wakeUpBackend(): Promise<void> {
  await fetchOrThrow(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/health-service/wakeup`
  );
}

export async function attemptQuestion<T extends AttemptResponse>(
  attemptRequest: AttemptRequest,
  getToken: () => Promise<string | null>
): Promise<T> {
  validateAttemptRequestOrDie(attemptRequest);

  const token = await getToken();
  assert(token !== null, "Token was null when attempting question");

  return await fetchOrThrow(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/language-test-service/questions/attempt`,
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

export async function fetchQuestions(
  language: string,
  getToken: () => Promise<string | null>
): Promise<QuestionDTO[]> {
  assert(language.trim().length > 0, "language is required");

  const token = await getToken();
  assert(token !== null, "Token was null when fetching questions");

  const encodedLanguage = encodeURIComponent(language);
  const url = `${process.env.NEXT_PUBLIC_API_BASE_URL}/language-test-service/questions?language=${encodedLanguage}`;

  const questions = await fetchOrThrow<QuestionDTO[]>(url, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  shuffleInPlace(questions);

  return questions;
}

export async function fetchQuestionAsset(
  sentenceID: string,
  assetType: AssetType
): Promise<Blob> {
  switch (assetType) {
    case "audio": {
      const audioURL = `${process.env.NEXT_PUBLIC_SPEECH_RECORDINGS_URL_PREFIX}/${sentenceID}.mp3`;
      const response = await fetch(audioURL);
      if (!response.ok) {
        throw new Error(`Failed to fetch audio for question ${sentenceID}`);
      }
      return response.blob();
    }
    default:
      throw new Error(`Unknown asset type: ${assetType}`);
  }
}

export function getQuestionAssetTypes(questionType: QuestionType): AssetType[] {
  switch (questionType) {
    case "FillInTheBlanks":
      return ["audio"];
    default:
      return [];
  }
}

function validateAttemptRequestOrDie(
  attemptRequest: AttemptRequest
): asserts attemptRequest is AttemptRequest {
  assert(attemptRequest.sentenceID.trim().length > 0, "sentenceID is blank");
  assert(
    attemptRequest.questionType.trim().length > 0,
    "questionType is blank"
  );
}
