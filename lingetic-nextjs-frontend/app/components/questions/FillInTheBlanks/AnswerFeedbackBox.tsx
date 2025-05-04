import type { ReactNode } from "react";

interface AnswerFeedbackBoxProps {
  children: ReactNode;
  className?: string;
}

export default function AnswerFeedbackBox({
  children,
  className = "",
}: AnswerFeedbackBoxProps) {
  return (
    <div
      className={`mt-2 text-sm border rounded-lg p-3 bg-gray-50 ${className}`.trim()}
    >
      {children}
    </div>
  );
}
