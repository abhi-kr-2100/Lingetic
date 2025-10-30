export type QuestionType = "FillInTheBlanks" | "Translation";
export type AssetType = "audio";
export type AttemptStatus = "Success" | "Failure";
export type Language =
  | "English"
  | "French"
  | "Turkish"
  | "Swedish"
  | "JapaneseModifiedHepburn"
  | "German";

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
  translateFromLanguage: Language;
  translateToLanguage: Language;
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
