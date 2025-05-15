import Question from "./Question";
import Result from "./Result";
import assert from "@/utilities/assert";

import type {
  SourceToTargetTranslationAttemptResponse,
  SourceToTargetTranslationQuestion,
} from "@/utilities/api-types";
import type { ChangeEvent, KeyboardEvent } from "react";

interface MainComponentProps {
  isChecked: boolean;
  attemptResponse: SourceToTargetTranslationAttemptResponse | undefined;
  question: SourceToTargetTranslationQuestion;
  answer: string;
  handleInputChange: (e: ChangeEvent<HTMLInputElement>) => void;
  handleInputKeyDown: (e: KeyboardEvent<HTMLInputElement>) => void;
  isChecking: boolean;
}

export default function MainComponent({
  isChecked,
  attemptResponse,
  question,
  answer,
  handleInputChange,
  handleInputKeyDown,
  isChecking,
}: MainComponentProps) {
  return (
    <div className="flex flex-col gap-4">
      {!isChecked ? (
        <div className="text-xl font-semibold">
          <Question
            question={question}
            value={answer}
            onChange={handleInputChange}
            onKeyDown={handleInputKeyDown}
            disabled={isChecked || isChecking}
          />
        </div>
      ) : attemptResponse !== undefined ? (
        <Result question={question} attemptResponse={attemptResponse} />
      ) : (
        <>
          {assert(false, "attemptResponse is undefined after successful check")}
        </>
      )}
    </div>
  );
}
