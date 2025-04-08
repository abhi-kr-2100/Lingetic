import { LanguageProperty } from "@/app/languages/constants";
import LanguageCard from "./LanguageCard";

interface LanguageListProps {
  languages: LanguageProperty[];
}

export default function LanguageList({ languages }: LanguageListProps) {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 max-w-5xl mx-auto">
      {languages.map((language) => (
        <LanguageCard key={language.id} language={language} />
      ))}
    </div>
  );
}
