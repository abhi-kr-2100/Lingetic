import type { WordExplanation } from "@/utilities/api-types";
import Chip from "./Chip";

interface WordExplanationHoverProps {
  word: string;
  explanation: WordExplanation;
}

export default function WordExplanationHover({
  word,
  explanation,
}: WordExplanationHoverProps) {
  return (
    <span className="relative group cursor-pointer px-2 py-1 rounded bg-skin-fill-accent hover:bg-skin-fill-accent/70 transition-colors mx-1">
      <span className="border-b-[2.5px] border-dotted border-skin-base text-skin-base">
        {word}
      </span>
      <div className="absolute left-1/2 -translate-x-1/2 mt-2 z-10 hidden group-hover:flex flex-col min-w-[200px] max-w-xs bg-white border border-gray-300 rounded shadow-lg p-3 text-xs text-gray-900 whitespace-pre-line">
        <div className="mb-2 font-bold text-sm">{explanation.comment}</div>
        <div className="flex flex-wrap gap-1 mt-2">
          {explanation.properties.map((property, idx) => (
            <Chip key={idx} text={property} />
          ))}
        </div>
      </div>
    </span>
  );
}
