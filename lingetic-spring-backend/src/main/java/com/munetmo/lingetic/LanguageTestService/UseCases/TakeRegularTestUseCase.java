package com.munetmo.lingetic.LanguageTestService.UseCases;

import java.util.List;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.*;

public class TakeRegularTestUseCase {
        private static final List<QuestionDTO> QUESTIONS = List.of(
                        new FillInTheBlanksQuestionDTO("1", "The cat ____ lazily on the windowsill.",
                                        "Past tense of 'stretch' - to extend one's body"),
                        new FillInTheBlanksQuestionDTO("2", "She ____ her keys on the kitchen counter.",
                                        "Past tense of 'leave' - to put something somewhere and forget it"),
                        new FillInTheBlanksQuestionDTO("3", "The children ____ excitedly when they saw the presents.",
                                        "Past tense of 'gasp' - to suddenly take a breath in surprise"),
                        new FillInTheBlanksQuestionDTO("4", "The sun ____ brightly in the clear blue sky.",
                                        "Past tense of 'shine' - to emit light"),
                        new FillInTheBlanksQuestionDTO("5", "He ____ the delicious meal in just ten minutes.",
                                        "Past tense of 'prepare' - to make something ready"),
                        new FillInTheBlanksQuestionDTO("6", "The birds ____ south for the winter.",
                                        "Past tense of 'fly' - to move through the air with wings"),
                        new FillInTheBlanksQuestionDTO("7", "The teacher ____ the students for their hard work.",
                                        "Past tense of 'praise' - to express approval or admiration"),
                        new FillInTheBlanksQuestionDTO("8", "The old car ____ to a stop at the red light.",
                                        "Past tense of 'come' - to move towards something"),
                        new FillInTheBlanksQuestionDTO("9", "They ____ through the forest for hours.",
                                        "Past tense of 'walk' - to move on foot"),
                        new FillInTheBlanksQuestionDTO("10", "The audience ____ loudly after the performance.",
                                        "Past tense of 'clap' - to strike the palms of one's hands together"));

        public List<QuestionDTO> execute() {
                return QUESTIONS;
        }
}
