interface NextButtonProps {
  onNext: () => void;
  isLastQuestion: boolean;
  ref?: React.RefObject<HTMLButtonElement | null>;
}

export default function NextButton({
  onNext,
  isLastQuestion,
  ref,
}: NextButtonProps) {
  return (
    <button
      ref={ref}
      autoFocus
      type="button"
      onClick={onNext}
      className="bg-[#2563eb] text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors flex items-center"
    >
      {isLastQuestion ? "Finish" : "Next"}
    </button>
  );
}
