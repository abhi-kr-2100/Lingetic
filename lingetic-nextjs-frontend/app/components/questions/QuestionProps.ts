import type { Question } from "@/utilities/api-types";

export default interface QuestionProps {
  question: Question;
  onAnswerSubmit?: () => void;
}
