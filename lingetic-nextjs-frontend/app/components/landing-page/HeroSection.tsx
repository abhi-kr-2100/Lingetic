import LanguageList from "./LanguageList";
import { LanguageProperty } from "@/app/languages/constants";

interface HeroSectionProps {
  languages: LanguageProperty[];
}

export default function HeroSection({ languages }: HeroSectionProps) {
  return (
    <section className="relative flex flex-col items-center justify-center px-4 md:px-8 lg:px-24 py-16">
      <div className="absolute inset-0 -z-10 bg-gradient-to-b from-blue-50 to-white" />
      <div className="max-w-4xl mx-auto text-center">
        <h1 className="text-4xl md:text-5xl lg:text-7xl font-bold mb-6 text-[#374151] tracking-tight">
          Learn Languages for <span className="text-[#2563eb]">Real-World</span>{" "}
          Conversations
        </h1>
        <p className="text-lg md:text-xl lg:text-2xl mb-10 text-[#374151] max-w-2xl mx-auto">
          AI-powered language learning that prepares you to speak confidently
          with native speakers.
        </p>
        <LanguageList
          languages={languages.filter((language) => language.isSupported)}
        />
      </div>
    </section>
  );
}
