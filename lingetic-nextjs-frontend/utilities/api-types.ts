export type QuestionType = "FillInTheBlanks" | "SourceToTargetTranslation";
export type AssetType = "audio";
export type AttemptStatus = "Success" | "Failure";

export interface Question {
  id: string;
  questionType: QuestionType;
}

export interface FillInTheBlanksQuestion extends Question {
  questionType: "FillInTheBlanks";
  language: string;
  text: string;
  hint: string;
}

export interface SourceToTargetTranslationQuestion extends Question {
  questionType: "SourceToTargetTranslation";
  language: string;
  translation: string;
}

export interface AttemptRequest {
  questionType: QuestionType;
  questionID: string;
}

export interface FillInTheBlanksAttemptRequest extends AttemptRequest {
  userResponse: string;
}

export interface SourceToTargetTranslationAttemptRequest
  extends AttemptRequest {
  userResponse: string;
}

export interface AttemptResponse {
  questionType: QuestionType;
  attemptStatus: AttemptStatus;
  sourceWordExplanations: WordExplanation[];
}

export interface FillInTheBlanksAttemptResponse extends AttemptResponse {
  correctAnswer: string;
}

export interface SourceToTargetTranslationAttemptResponse
  extends AttemptResponse {
  correctAnswer: string;
}

export interface WordExplanation {
  startIndex: number;
  word: string;
  properties: string[];
  comment: string;
}
