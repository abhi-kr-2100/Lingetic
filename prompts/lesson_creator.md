You'll be given a subset of a table of contents from a language learning book. Using that table of contents, you should design a detailed lesson.

The lesson should include the following in a JSON format:

{
    "objective": "A one-line description of what the lesson should accomplish.",
    "vocabulary": ["Word 1", "Word 2", "Word 3"], // An exhaustive list of all words that are taught in this lesson.
    "grammar": ["Rule 1", "Rule 2", "Rule 3"], // An exhaustive list of all grammar rules that are taught in this lesson.
    "sentenceTemplates": ["Template 1", "Template 2", "Template 3"], // An exhaustive list of all sentence templates that should be taught in this lesson. Some example sentence templates are: "<Pronoun> is a <Profession>.", "Are you a <Profession>?", etc.
}

Ensure that the grammar rules are detailed and comprehensive for the language. In case, a word can have multiple meanings, explain in the grammar rules, which meaning should be used in which context. Don't assume that the lesson is meant for beginners. Cover advanced topics in the lesson. Exhaustively cover all topics in the lesson from beginner to advanced academic level.
