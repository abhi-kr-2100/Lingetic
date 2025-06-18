export type QuestionType = "FillInTheBlanks" | "Translation";
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

export interface TranslationQuestionDTO extends QuestionDTO {
  questionType: "Translation";
  toTranslateText: string;
  translateFromLanguage: string;
  translateToLanguage: string;
}

export interface AttemptRequest {
  questionType: QuestionType;
  sentenceID: string;
}

export interface FillInTheBlanksAttemptRequest extends AttemptRequest {
  questionType: "FillInTheBlanks";
  userResponse: string;
}

export interface TranslationAttemptRequest extends AttemptRequest {
  questionType: "Translation";
  userResponse: string;
}

export interface AttemptResponse {
  questionType: QuestionType;
  attemptStatus: AttemptStatus;
  sourceWordExplanations: WordExplanation[];
}

export interface FillInTheBlanksAttemptResponse extends AttemptResponse {
  questionType: "FillInTheBlanks";
  correctAnswer: string;
}

export interface TranslationAttemptResponse extends AttemptResponse {
  questionType: "Translation";
  correctAnswer: string;
}

export interface WordExplanation {
  startIndex: number;
  word: string;
  properties: string[];
  comment: string;
}
