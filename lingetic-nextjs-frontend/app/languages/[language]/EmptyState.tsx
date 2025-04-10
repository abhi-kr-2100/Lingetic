import { BookOpen } from "lucide-react";
import { useRouter } from "next/navigation";

interface EmptyStateProps {
  language: string;
}

export function EmptyState({ language }: EmptyStateProps) {
  const router = useRouter();

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white flex items-center justify-center">
      <div className="bg-white p-8 rounded-xl shadow-lg max-w-md w-full text-center">
        <BookOpen className="h-12 w-12 text-[#2563eb] mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-[#374151] mb-2">
          No Question Lists Available
        </h2>
        <p className="text-[#374151] mb-6">
          {`We don't have any question lists for ${language} yet. Please check back later.`}
        </p>
        <button
          onClick={() => router.push("/languages")}
          className="bg-[#2563eb] text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors"
        >
          Back to Languages
        </button>
      </div>
    </div>
  );
}
