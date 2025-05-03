import type { AttemptStatus, Question } from "@/utilities/api-types";
import type { ReactNode } from "react";

export default interface QuestionProps {
  question: Question;
  afterAnswerCheck?: (attemptStatus?: AttemptStatus) => void;
  NextButton: ReactNode;
}
