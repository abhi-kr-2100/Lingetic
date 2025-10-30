import { Language } from "@/utilities/api-types";

export interface LanguageProperty {
  id: Language;
  name: string;
  image: string;
  isSupported: boolean;
}

export const languages: LanguageProperty[] = [
  {
    id: "French",
    name: "French",
    image: "/img/languages/french-flag.png",
    isSupported: true,
  },
  {
    id: "Swedish",
    name: "Swedish",
    image: "/img/languages/swedish-flag.png",
    isSupported: true,
  },
  {
    id: "Turkish",
    name: "Turkish",
    image: "/img/languages/turkish-flag.png",
    isSupported: false,
  },
  // {
  //   id: "Spanish",
  //   name: "Spanish",
  //   image: "/img/languages/spanish-flag.png",
  //   isSupported: false,
  // },
  // {
  {
    id: "German",
    name: "German",
    image: "/img/languages/german-flag.png",
    isSupported: true,
  },
  // {
  //   id: "Italian",
  //   name: "Italian",
  //   image: "/img/languages/italian-flag.png",
  //   isSupported: false,
  // },
  // {
  //   id: "Portuguese",
  //   name: "Portuguese",
  //   image: "/img/languages/portuguese-flag.png",
  //   isSupported: false,
  // },
  {
    id: "JapaneseModifiedHepburn",
    name: "Japanese (Romanized)",
    image: "/img/languages/japanese-flag.png",
    isSupported: true,
  },
  // {
  //   id: "Japanese",
  //   name: "Japanese",
  //   image: "/img/languages/japanese-flag.png",
  //   isSupported: false,
  // },
  // {
  //   id: "Korean",
  //   name: "Korean",
  //   image: "/img/languages/korean-flag.png",
  //   isSupported: false,
  // },
  // {
  //   id: "Chinese",
  //   name: "Chinese",
  //   image: "/img/languages/chinese-flag.png",
  //   isSupported: false,
  // },
  // {
  //   id: "Russian",
  //   name: "Russian",
  //   image: "/img/languages/russian-flag.png",
  //   isSupported: false,
  // },
  // {
  //   id: "Dutch",
  //   name: "Dutch",
  //   image: "/img/languages/dutch-flag.png",
  //   isSupported: false,
  // },
  // {
  //   id: "Danish",
  //   name: "Danish",
  //   image: "/img/languages/danish-flag.png",
  //   isSupported: false,
  // },
];

export const languageNameToCode: Record<Language, string> = {
  English: "en",
  French: "fr",
  Swedish: "sv",
  Turkish: "tr",
  JapaneseModifiedHepburn: "ja-hepburn",
  German: "de",
};

export const languageIDToName: Record<string, string> = Object.fromEntries(
  languages.map((lang) => [lang.id, lang.name])
);
