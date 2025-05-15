"use client";

import { useEffect, useState } from "react";
import assert from "@/utilities/assert";
import type QuestionProps from "../QuestionProps";
import type {
  SourceToTargetTranslationQuestion,
  SourceToTargetTranslationAttemptResponse,
} from "@/utilities/api-types";
import ActionButton from "./ActionButton";
import useTextUserAnswer from "../hooks/useTextUserAnswer";
import MainComponent from "./MainComponent";
import AnswerCheckStatus from "./AnswerCheckStatus";

interface SourceToTargetTranslationProps extends QuestionProps {
  question: SourceToTargetTranslationQuestion;
}

export default function SourceToTargetTranslation({
  question,
  afterAnswerCheck,
  NextButton,
}: SourceToTargetTranslationProps) {
  validateQuestionOrDie(question);

  const [attemptResponse, setAttemptResponse] = useState<
    SourceToTargetTranslationAttemptResponse | undefined
  >(undefined);

  useEffect(() => {
    setAttemptResponse(undefined);
  }, [question.id]);

  const { answer, setAnswer, checkAnswer, isChecking, isChecked, isError } =
    useTextUserAnswer<SourceToTargetTranslationAttemptResponse>(question);

  async function handleCheckAnswer() {
    const response = await checkAnswer();
    setAttemptResponse(response);
    afterAnswerCheck?.(response?.attemptStatus);
  }

  function handleInputChange(e: React.ChangeEvent<HTMLInputElement>) {
    setAnswer(e.target.value);
  }

  function handleInputKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter" && !isChecked) {
      void handleCheckAnswer();
    }
  }

  return (
    <div className="w-full p-6 flex flex-col gap-2 border border-gray-200 rounded-lg shadow-md">
      <MainComponent
        isChecked={isChecked}
        attemptResponse={attemptResponse}
        question={question}
        answer={answer}
        handleInputChange={handleInputChange}
        handleInputKeyDown={handleInputKeyDown}
        isChecking={isChecking}
      />
      <AnswerCheckStatus
        isError={isError}
        isChecked={isChecked}
        attemptResponse={attemptResponse}
        userAnswer={answer}
      />
      <ActionButton
        isChecked={isChecked}
        isChecking={isChecking}
        onCheck={handleCheckAnswer}
        NextButton={NextButton}
        value={answer}
      />
    </div>
  );
}

function validateQuestionOrDie(question: SourceToTargetTranslationQuestion) {
  assert(question != null, "question is null or undefined");
  assert(
    question.translation?.trim()?.length > 0,
    "question.translation is empty"
  );
  assert(
    question.questionType === "SourceToTargetTranslation",
    "question.questionType is not SourceToTargetTranslation"
  );
  assert(
    question.translation?.trim()?.length > 0,
    "question.translation is empty"
  );
}
