import type { AttemptStatus, Question } from "@/utilities/api-types";

export default interface QuestionProps {
  question: Question;
  afterAnswerCheck?: (attemptStatus?: AttemptStatus) => void;
}
