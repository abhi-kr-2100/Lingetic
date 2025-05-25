export type QuestionType = "FillInTheBlanks";
export type AssetType = "audio";
export type AttemptStatus = "Success" | "Failure";

export interface QuestionDTO {
  sentenceID: string;
  questionType: QuestionType;
}

export interface FillInTheBlanksQuestionDTO extends QuestionDTO {
  questionType: "FillInTheBlanks";
  text: string;
  hint: string;
}

export interface AttemptRequest {
  questionType: QuestionType;
  sentenceID: string;
}

export interface FillInTheBlanksAttemptRequest extends AttemptRequest {
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

export interface WordExplanation {
  startIndex: number;
  word: string;
  properties: string[];
  comment: string;
}
