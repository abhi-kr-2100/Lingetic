interface QuestionListHeaderProps {
  language: string;
}

export function QuestionListHeader({ language }: QuestionListHeaderProps) {
  return (
    <div className="flex items-center justify-between mb-8">
      <h1 className="text-3xl font-bold text-[#374151]">
        Question Lists for <span className="text-[#2563eb]">{language}</span>
      </h1>
    </div>
  );
}
