"use client";

import { useEffect, useState } from "react";
import assert from "@/utilities/assert";
import type QuestionProps from "../QuestionProps";
import type {
    TranslationQuestionDTO,
    TranslationAttemptResponse,
} from "@/utilities/api-types";
import ActionButton from "../components/ActionButton";
import AnswerCheckStatus from "../components/AnswerCheckStatus";
import MainComponent from "./MainComponent";
import useTextUserAnswer from "../hooks/useTextUserAnswer";

interface TranslationProps extends QuestionProps {
    question: TranslationQuestionDTO;
}

export default function Translation({
    question,
    afterAnswerCheck,
    NextButton,
}: TranslationProps) {
    validateQuestionOrDie(question);

    const [attemptResponse, setAttemptResponse] = useState<
        TranslationAttemptResponse | undefined
    >(undefined);

    useEffect(() => {
        setAttemptResponse(undefined);
    }, [question.sentenceID]);

    const { answer, setAnswer, checkAnswer, isChecking, isChecked, isError } =
        useTextUserAnswer<TranslationAttemptResponse>(question);

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
                question={question}
                value={answer}
            />
        </div>
    );
}

function validateQuestionOrDie(question: TranslationQuestionDTO) {
    assert(question != null, "question is null or undefined");
    assert(
        question.sentenceID?.trim()?.length > 0,
        "question.sentenceID is empty"
    );
    assert(
        question.questionType === "Translation",
        "question.questionType is not Translation"
    );
    assert(
        question.toTranslateText?.trim()?.length > 0,
        "question.toTranslateText is empty"
    );
    assert(
        question.translateFromLanguage?.trim()?.length > 0,
        "question.translateFromLanguage is empty"
    );
    assert(
        question.translateToLanguage?.trim()?.length > 0,
        "question.translateToLanguage is empty"
    );
}
