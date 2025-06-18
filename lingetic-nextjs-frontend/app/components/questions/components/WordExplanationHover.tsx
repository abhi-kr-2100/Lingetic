import { useState } from "react";
import { useFloating, shift, autoUpdate } from "@floating-ui/react";

import Chip from "./Chip";

import type { WordExplanation } from "@/utilities/api-types";

interface WordExplanationHoverProps {
  word: string;
  explanation: WordExplanation;
  className?: string;
}

export default function WordExplanationHover({
  word,
  explanation,
  className,
}: WordExplanationHoverProps) {
  const [open, setOpen] = useState(false);
  const { refs, floatingStyles } = useFloating({
    middleware: [shift()],
    whileElementsMounted: autoUpdate,
    placement: "bottom",
  });

  return (
    <span
      className={`cursor-pointer ${className ?? ""}`}
      ref={refs.setReference}
      onMouseEnter={() => setOpen(true)}
      onMouseLeave={() => setOpen(false)}
      tabIndex={0}
      onFocus={() => setOpen(true)}
      onBlur={() => setOpen(false)}
    >
      <span className="border-b-[0.5px] border-dotted border-skin-base text-skin-base">
        {word}
      </span>
      {open && (
        <div
          ref={refs.setFloating}
          style={floatingStyles}
          className="z-10 flex flex-col min-w-[200px] max-w-xs bg-white border border-gray-300 rounded shadow-lg p-3 text-xs text-gray-900 whitespace-pre-line"
        >
          <div className="mb-2 font-bold text-sm">{explanation.comment}</div>
          <div className="flex flex-wrap gap-1 mt-2">
            {explanation.properties.map((property, idx) => (
              <Chip key={idx} text={property} />
            ))}
          </div>
        </div>
      )}
    </span>
  );
}
