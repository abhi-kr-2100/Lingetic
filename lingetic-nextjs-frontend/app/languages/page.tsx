import LanguagePlayCard from "../components/LanguagePlayCard";
import { Globe } from "lucide-react";
import { languages } from "./constants";
import Link from "next/link";

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
          <div className="text-center">
            <p className="text-[#374151] mb-4">
              {"Don't see the language you want to learn?"}
            </p>
            <Link
              href={`mailto:${process.env.NEXT_PUBLIC_FEEDBACK_EMAIL_ADDRESS}?subject=Request%20a%20language`}
              target="_blank"
              className="bg-white border-2 border-[#2563eb] text-[#2563eb] px-6 py-3 rounded-lg font-medium hover:bg-blue-50 transition-colors"
            >
              Request a Language
            </Link>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-7xl mx-auto">
          {languages
            .filter((language) => language.isSupported)
            .map((language) => (
              <LanguagePlayCard
                key={language.id}
                id={language.id}
                name={language.name}
                image={language.image}
              />
            ))}
        </div>
      </div>
    </div>
  );
}
