import React from "react";
import { diffChars } from "diff";

interface DiffHighlightProps {
  userAnswer: string;
  correctAnswer: string;
}

const DiffHighlight: React.FC<DiffHighlightProps> = ({
  userAnswer,
  correctAnswer,
}) => {
  const diff = diffChars(correctAnswer, userAnswer);

  return (
    <span>
      {diff.map((part, idx) => {
        if (part.added) {
          return (
            <span key={idx} className="bg-green-100 text-green-700">
              {part.value}
            </span>
          );
        } else if (part.removed) {
          return (
            <span key={idx} className="bg-red-100 text-red-700 line-through">
              {part.value}
            </span>
          );
        } else {
          return <span key={idx}>{part.value}</span>;
        }
      })}
    </span>
  );
};

export default DiffHighlight;
