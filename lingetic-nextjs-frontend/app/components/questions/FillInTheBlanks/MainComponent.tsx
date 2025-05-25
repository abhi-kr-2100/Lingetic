import Question from "./Question";
import Result from "./Result";
import assert from "@/utilities/assert";

import type {
  FillInTheBlanksAttemptResponse,
  FillInTheBlanksQuestionDTO,
} from "@/utilities/api-types";
import type { ChangeEvent, KeyboardEvent } from "react";

interface MainComponentProps {
  isChecked: boolean;
  attemptResponse: FillInTheBlanksAttemptResponse | undefined;
  question: FillInTheBlanksQuestionDTO;
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
      <div className="text-xl font-semibold">
        {!isChecked ? (
          <Question
            question={question}
            value={answer}
            onChange={handleInputChange}
            onKeyDown={handleInputKeyDown}
            disabled={isChecked || isChecking}
          />
        ) : attemptResponse !== undefined ? (
          <Result question={question} attemptResponse={attemptResponse} />
        ) : (
          <>
            {assert(
              false,
              "attemptResponse is undefined after successful check"
            )}
          </>
        )}
      </div>
      {question.hint.length > 0 && (
        <p className="text-skin-base italic">{question.hint}</p>
      )}
    </div>
  );
}
