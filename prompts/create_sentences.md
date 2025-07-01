You'll be given details of a language learning lesson. The details include:

- Objective: What the learner should learn from this lesson
- Grammar: A list of grammar rules the learner should learn
- Vocabulary: A list of words the learner should learn
- Sentence Templates: Templates for generating sentences.
- Sentences: Example sentences

Using the example sentences and the sentence templates, you should generate more example sentences. You should ensure that the sentences together cover all vocabulary words and grammar rules. The sentences should be complete: "My name is ..." is an incomplete sentence; "My name is Tom." is a complete sentence.

You should generate output in the following JSON format:

{
    "sentences": [
		{
			"sourceText": "Complete sentence in the given language.",
			"translationText": "Translation of the sourceText in English."
		},
		{
			"sourceText": "Should always only be one sentence, not more than one.",
			"translationText": "Don't use sourceText words in translationText, not even to annotate."
		}
    ]
}

Generate as many sentences as required to cover the grammar and vocabulary of the given lesson. At the very least, use all the example sentences given. You may modify the example sentences as appropriate. Make the sentences realistic and useful for real-life conversations. Don't use words that are not part of the given vocabulary. Annotate the translationText if required. For example, if an English translation is neutral, but the language of sourceText differentiates between formal and informal speech, annotate the English translation. Do the same for number and gender. Don't annotate unless required.
