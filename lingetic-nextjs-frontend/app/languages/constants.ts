export interface LanguageProperty {
  id: string;
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
  {
    id: "Spanish",
    name: "Spanish",
    image: "/img/languages/spanish-flag.png",
    isSupported: false,
  },
  {
    id: "German",
    name: "German",
    image: "/img/languages/german-flag.png",
    isSupported: false,
  },
  {
    id: "Italian",
    name: "Italian",
    image: "/img/languages/italian-flag.png",
    isSupported: false,
  },
  {
    id: "Portuguese",
    name: "Portuguese",
    image: "/img/languages/portuguese-flag.png",
    isSupported: false,
  },
  {
    id: "JapaneseModifiedHepburn",
    name: "Japanese 1",
    image: "/img/languages/japanese-flag.png",
    isSupported: true,
  },
  {
    id: "Japanese",
    name: "Japanese 2",
    image: "/img/languages/japanese-flag.png",
    isSupported: false,
  },
  {
    id: "Korean",
    name: "Korean",
    image: "/img/languages/korean-flag.png",
    isSupported: false,
  },
  {
    id: "Chinese",
    name: "Chinese",
    image: "/img/languages/chinese-flag.png",
    isSupported: false,
  },
  {
    id: "Russian",
    name: "Russian",
    image: "/img/languages/russian-flag.png",
    isSupported: false,
  },
  {
    id: "Dutch",
    name: "Dutch",
    image: "/img/languages/dutch-flag.png",
    isSupported: false,
  },
  {
    id: "Danish",
    name: "Danish",
    image: "/img/languages/danish-flag.png",
    isSupported: false,
  },
];

export const languageNameToCode: Record<string, string> = {
  French: "fr",
  Swedish: "sv",
  Turkish: "tr",
  Spanish: "es",
  German: "de",
  Italian: "it",
  Portuguese: "pt",
  Japanese: "ja",
  JapaneseModifiedHepburn: "ja-hepburn",
  Korean: "ko",
  Chinese: "zh",
  Russian: "ru",
  Dutch: "nl",
  Danish: "da",
};
