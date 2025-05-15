import type { ReactNode } from "react";

interface ActionButtonProps {
  isChecked: boolean;
  isChecking: boolean;
  onCheck: () => void;
  NextButton: ReactNode;
  value: string;
}

export default function ActionButton({
  isChecked,
  isChecking,
  onCheck,
  NextButton,
  value,
}: ActionButtonProps) {
  return (
    <div className="flex justify-end items-center gap-3">
      {isChecked ? (
        NextButton
      ) : (
        <button
          type="button"
          disabled={isChecking}
          onClick={onCheck}
          className="bg-skin-button-primary text-white px-6 py-3 rounded-lg font-medium hover:bg-skin-button-primary-hover transition-colors flex items-center"
        >
          {isChecking
            ? "Checking..."
            : value.trim().length === 0
            ? "Skip"
            : "Check"}
        </button>
      )}
    </div>
  );
}
