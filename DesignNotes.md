# Design Notes

This document contains all the design decisions made while developing Lingetic. Additionally, any central piece of information appears here.

## What is Lingetic?

`Lingetic` is a language learning app that is similar to Duolingo in many ways. What differentiates Lingetic from others is its focus on speed; given the choice of making learning fun and slow or fast and boring, Lingetic would choose fast and boring, although the ideal goal is fast and fun.

## MVP and Future Product Direction

This section, written before any code for Lingetic has been written, specifies a minimal viable version of Lingetic. It also outlines a rough future goal for the product. Any implementation should ensure that it is compatible with the future product direction.

### Version 0

The goal of version 0 is to enable the developer to get started, and to produce something tangible as quickly as possible.

* Fill-in-the-blank type questions, similar to those on Clozemaster: Lingetic will eventually support a variety of question types; however, fill-in-the-blanks are the easiest since data is easily available from Tatoeba.
* Support for learning multiple languages from the source language English: supporting multiple target languages should not be too difficult compared to supporting just one target language. Supporting multiple source languages can be more work, and will be taken up in the future.

### Version 1

The goal of version 1 is to make version 0 more useful.

* Ordering of questions based on language concepts, e.g., nouns should be learned before verbs.

### Version 2

Version 2 personalizes Lingetic on a per user basis.

* Support for multiple users.
* Intelligent scheduling based on user's attempts.

Once version 2 is complete, a thorough testing of the platform should be conducted to get a feel for how useful it is as a language learning tool. As a benchmark, at this point Lingetic should be able to serve as a sequel to Duolingo. It should be deployed to users for testing.

From here onwards, user needs should take priority over the tentative product direction.

### Version 3

At version 2, Lingetic is useful for advancing knowledge in a language, but not for learning a language from scratch. Version 3 should address these deficiencies.

* Support for more kinds of questions: identify the word from a picture, translate full sentences, correct use of articles and other grammatical particles.
* Language roadmap from beginner to advanced.

After version 3, users should be able to start with Lingetic as their language learning app. They should be able to acquire enough skills to read and write their target language at an intermediate level.

### Version 4

Reading and writing is a very different skill from aural comprehension and speaking. Version 4 will address these issues.

* Support for question types that require hearing and speaking.
* Multi step exercises: talk to an AI actor, listen through an interactive story, etc.

### Version 5

* Users should be able to bring their own data: YouTube videos, films, PDFs, web pages, etc., and learn from them.

## Tech Stack

Ligentic uses Next.js for the frontend, using pnpm and TypeScript.
