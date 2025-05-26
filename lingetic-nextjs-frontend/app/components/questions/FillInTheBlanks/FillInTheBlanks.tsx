"use client";

import { useEffect, useState } from "react";

import assert from "@/utilities/assert";
import type QuestionProps from "../QuestionProps";
import type {
  FillInTheBlanksQuestionDTO,
  FillInTheBlanksAttemptResponse,
} from "@/utilities/api-types";

import ActionButton from "./ActionButton";
import AnswerCheckStatus from "./AnswerCheckStatus";
import MainComponent from "./MainComponent";
import useTextUserAnswer from "../hooks/useTextUserAnswer";

interface FillInTheBlanksProps extends QuestionProps {
  question: FillInTheBlanksQuestionDTO;
}

export default function FillInTheBlanks({
  question,
  afterAnswerCheck,
  NextButton,
}: FillInTheBlanksProps) {
  validateQuestionOrDie(question);

  const [attemptResponse, setAttemptResponse] = useState<
    FillInTheBlanksAttemptResponse | undefined
  >(undefined);

  useEffect(() => {
    setAttemptResponse(undefined);
  }, [question.sentenceID]);

  const { answer, setAnswer, checkAnswer, isChecking, isChecked, isError } =
    useTextUserAnswer<FillInTheBlanksAttemptResponse>(question);

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
    <div className="w-full p-6 flex flex-col gap-2  border border-gray-200 rounded-lg shadow-md">
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
        question={question}
        value={answer}
      />
    </div>
  );
}

function validateQuestionOrDie(question: FillInTheBlanksQuestionDTO) {
  assert(question != null, "question is null or undefined");
  assert(
    question.sentenceID?.trim()?.length > 0,
    "question.sentenceID is empty"
  );
  assert(
    question.questionType === "FillInTheBlanks",
    "question.questionType is not FillInTheBlanks"
  );
  assert(question.text?.trim()?.length > 0, "question.text is empty");
  assert(
    question.text.includes("_"),
    "question.text does not contain any blank"
  );
  assert(
    question.text?.match(/_+/g)?.length === 1,
    "question.text contains more than one blank"
  );
}
