export type QuestionType = "FillInTheBlanks" | "SourceToTargetTranslation";
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

export interface SourceToTargetTranslationQuestionDTO extends QuestionDTO {
  questionType: "SourceToTargetTranslation";
  sourceText: string;
  sourceLanguage: string;
  targetLanguage: string;
}

export interface AttemptRequest {
  questionType: QuestionType;
  sentenceID: string;
}

export interface FillInTheBlanksAttemptRequest extends AttemptRequest {
  questionType: "FillInTheBlanks";
  userResponse: string;
}

export interface SourceToTargetTranslationAttemptRequest extends AttemptRequest {
  questionType: "SourceToTargetTranslation";
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

export interface SourceToTargetTranslationAttemptResponse extends AttemptResponse {
  questionType: "SourceToTargetTranslation";
  correctAnswer: string;
}

export interface WordExplanation {
  startIndex: number;
  word: string;
  properties: string[];
  comment: string;
}
