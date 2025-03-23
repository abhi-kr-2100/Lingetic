import LanguageCard from "../components/LanguageCard";

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
    <div className="container mx-auto p-4">
      <h1 className="text-skin-base text-3xl font-bold mb-6">
        Available Languages
      </h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-6">
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
    </div>
  );
}
