import { useRouter } from "next/navigation";
import type { QuestionList } from "@/utilities/api-types";

interface QuestionListGridProps {
  questionLists: QuestionList[];
  language: string;
}

export function QuestionListGrid({
  questionLists,
  language,
}: QuestionListGridProps) {
  const router = useRouter();

  return (
    <div className="grid gap-4">
      {questionLists.map((list: QuestionList) => (
        <button
          key={list.id}
          onClick={() => router.push(`/languages/${language}/${list.id}`)}
          className="bg-white p-6 rounded-xl shadow-lg hover:shadow-xl transition-shadow text-left w-full"
        >
          <h2 className="text-xl font-semibold text-[#374151] mb-2">
            {list.name}
          </h2>
        </button>
      ))}
    </div>
  );
}
