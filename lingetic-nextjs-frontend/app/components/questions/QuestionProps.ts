import type { AttemptStatus, QuestionDTO } from "@/utilities/api-types";
import type { ReactNode } from "react";

export default interface QuestionProps {
  question: QuestionDTO;
  afterAnswerCheck?: (attemptStatus?: AttemptStatus) => void;
  NextButton: ReactNode;
}
