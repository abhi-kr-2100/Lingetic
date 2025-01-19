import LanguageCard from "../components/LanguageCard";

const languages = [
  {
    id: "turkish",
    name: "Turkish",
    description: "Learn Turkish, the language of Turkey and Northern Cyprus.",
    image: undefined,
  },
  {
    id: "spanish",
    name: "Spanish",
    description: "Learn Spanish, one of the world's most spoken languages.",
    image: undefined,
  },
  {
    id: "finnish",
    name: "Finnish",
    description:
      "Learn Finnish, the language of Finland and its unique culture.",
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
