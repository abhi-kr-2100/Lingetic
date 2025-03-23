import LanguageCard from "../components/LanguageCard";
import { Globe } from "lucide-react";

export interface LanguageProperty {
  id: string;
  name: string;
  description: string;
  image?: string;
}

const languages: LanguageProperty[] = [
  {
    id: "English",
    name: "English",
    description: "Learn English, the most widely spoken language in the world.",
    image: undefined,
  },
  {
    id: "Turkish",
    name: "Turkish",
    description:
      "Learn Turkish, the official language of Turkey and a gateway to rich cultural heritage.",
    image: undefined,
  },
];

export default function Languages() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <div className="container mx-auto px-4 py-16">
        <div className="text-center mb-16">
          <div className="flex items-center justify-center mb-4">
            <Globe className="h-10 w-10 text-[#2563eb] mr-3" />
            <h1 className="text-4xl md:text-5xl font-bold text-[#374151]">
              Available Languages
            </h1>
          </div>
          <p className="text-xl text-[#374151] max-w-2xl mx-auto">
            Choose a language to start your journey to fluency with Lingetic's
            AI-powered learning
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-7xl mx-auto">
          {languages.map((language) => (
            <LanguageCard
              key={language.id}
              id={language.id}
              name={language.name}
              description={language.description}
              image={language.image}
            />
          ))}
        </div>

        <div className="mt-16 text-center">
          <p className="text-[#374151] mb-4">
            Don't see the language you want to learn?
          </p>
          <button className="bg-white border-2 border-[#2563eb] text-[#2563eb] px-6 py-3 rounded-lg font-medium hover:bg-blue-50 transition-colors">
            Request a Language
          </button>
        </div>
      </div>
    </div>
  );
}
