type QuestionType = "FillInTheBlanks";
type AttemptStatus = "Success" | "Failure";

export interface Question {
  id: string;
  type: QuestionType;
}

export interface FillInTheBlanksQuestion extends Question {
  type: "FillInTheBlanks";
  text: string;
  hint: string;
}

export interface AttemptRequest {
  type: QuestionType;
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
  comment: string;
  correctAnswer: string;
}
