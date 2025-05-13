export type QuestionType = "FillInTheBlanks";
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

export interface AttemptRequest {
  questionType: QuestionType;
  questionID: string;
}

export interface FillInTheBlanksAttemptRequest extends AttemptRequest {
  userResponse: string;
}

export interface AttemptResponse {
  questionType: QuestionType;
  attemptStatus: AttemptStatus;
}

export interface FillInTheBlanksAttemptResponse extends AttemptResponse {
  correctAnswer: string;
  explanation: WordExplanation[];
}

export interface WordExplanation {
  startIndex: number;
  word: string;
  properties: string[];
  comment: string;
}
