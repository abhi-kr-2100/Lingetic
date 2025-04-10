import Link from "next/link";
import Image from "next/image";
import { LanguageProperty } from "@/app/languages/constants";

interface LanguageCardProps {
  language: LanguageProperty;
}

export default function LanguageCard({ language }: LanguageCardProps) {
  const url = `/languages/${language.id}`;

  return (
    <Link href={url}>
      <div className="flex flex-col items-center p-4 border border-gray-100 hover:shadow-lg transition-shadow duration-300 cursor-pointer rounded-lg">
        <div className="w-16 h-16 sm:w-20 sm:h-20 rounded-full overflow-hidden flex-shrink-0 mb-3">
          <Image
            src={language.image}
            alt={language.name}
            width={80}
            height={80}
            className="w-full h-full object-cover"
          />
        </div>
        <h3 className="text-lg font-semibold text-[#374151] text-center">
          {language.name}
        </h3>
      </div>
    </Link>
  );
}
