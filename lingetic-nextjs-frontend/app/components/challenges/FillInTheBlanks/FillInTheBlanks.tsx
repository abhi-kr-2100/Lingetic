"use client";

import useUserAnswer from "./useUserAnswer";

interface FillInTheBlanksProps {
  question: {
    id: string;
    type: "FillInTheBlanks";
    text: string;
    hint: string;
  };
}

export default function FillInTheBlanks({ question }: FillInTheBlanksProps) {
  const { answer, setAnswer, checkAnswer, isChecked, isError, result } =
    useUserAnswer(question.id);

  return (
    <div className="shadow-lg rounded-lg p-6">
      <p className="text-skin-base text-xl mb-4">{question.text}</p>
      <p className="text-skin-base mb-4">Hint: {question.hint}</p>
      <input
        type="text"
        value={answer}
        onChange={(e) => setAnswer(e.target.value)}
        className="w-full p-2 border rounded mb-4"
        disabled={isChecked}
      />
      {!isChecked && (
        <button
          onClick={checkAnswer}
          className="bg-skin-button-primary text-skin-inverted px-4 py-2 rounded transition-colors"
        >
          Check
        </button>
      )}
      {isChecked && result && (
        <div>
          <p
            className={`mb-4 ${
              result.status === "success"
                ? "text-skin-success"
                : "text-skin-error"
            }`}
          >
            {result.status === "success" ? "Correct!" : "Incorrect."}
            {result.comment && ` ${result.comment}`}
          </p>
          {result.status === "failure" && result.answer && (
            <p>Correct answer: {result.answer}</p>
          )}
        </div>
      )}
      {isError && <p>An error occurred! Please try again after some time.</p>}
    </div>
  );
}
