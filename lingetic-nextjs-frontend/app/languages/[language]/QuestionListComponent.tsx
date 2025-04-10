"use client";

import { useParams } from "next/navigation";
import { LoadingState } from "./LoadingState";
import { ErrorState } from "./ErrorState";
import { EmptyState } from "./EmptyState";
import { QuestionListGrid } from "./QuestionListGrid";
import { QuestionListHeader } from "./QuestionListHeader";
import useQuestionLists from "./useQuestionLists";

export default function QuestionListComponent() {
  const params = useParams();
  const language = params.language as string;
  const result = useQuestionLists({ language });

  if (result.isLoading) return <LoadingState />;
  if (result.isError) return <ErrorState />;
  if (!result.hasLists || result.lists.length === 0)
    return <EmptyState language={language} />;

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white p-8">
      <div className="max-w-4xl mx-auto">
        <QuestionListHeader language={language} />
        <QuestionListGrid questionLists={result.lists} language={language} />
      </div>
    </div>
  );
}
