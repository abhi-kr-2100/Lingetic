package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JapaneseLanguageModelTest {
    private JapaneseLanguageModel model;

    @BeforeEach
    void setUp() throws IOException {
        model = new JapaneseLanguageModel();
    }

    @Test
    void getLanguageShouldReturnJapanese() {
        assertEquals(Language.Japanese, model.getLanguage());
    }

    @Test
    void tokenizeShouldHandleEmptyStrings() {
        assertTrue(model.tokenize("").isEmpty());
        assertTrue(model.tokenize(" ").isEmpty());
    }

    @Test
    void tokenizeShouldTokenizeSimpleSentences() {
        List<Token> tokens = model.tokenize("私は猫です。");
        assertEquals(5, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("私", tokens.get(0).value());
        assertEquals(1, tokens.get(0).sequenceNumber());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("は", tokens.get(1).value());
        assertEquals(2, tokens.get(1).sequenceNumber());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("猫", tokens.get(2).value());
        assertEquals(3, tokens.get(2).sequenceNumber());

        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("です", tokens.get(3).value());
        assertEquals(4, tokens.get(3).sequenceNumber());

        assertEquals(TokenType.Punctuation, tokens.get(4).type());
        assertEquals("。", tokens.get(4).value());
        assertEquals(5, tokens.get(4).sequenceNumber());
    }

    void tokenizeShouldIgnoreWhitespaces() {
        var tokens = model.tokenize("で  す");

        assertEquals(1, tokens.size());
        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("です", tokens.get(0).value());
    }

    @Test
    void tokenizeShouldHandleNumbers() {
        List<Token> tokens = model.tokenize("私は25歳です。");

        assertEquals(6, tokens.size());
        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("私", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("は", tokens.get(1).value());

        assertEquals(TokenType.Number, tokens.get(2).type());
        assertEquals("25", tokens.get(2).value());

        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("歳", tokens.get(3).value());

        assertEquals(TokenType.Word, tokens.get(4).type());
        assertEquals("です", tokens.get(4).value());

        assertEquals(TokenType.Punctuation, tokens.get(5).type());
        assertEquals("。", tokens.get(5).value());
    }

    @Test
    void tokenizeShouldHandleMixedScripts() {
        List<Token> tokens = model.tokenize("彼はJavaプログラマーです。");

        assertEquals(6, tokens.size());
        assertEquals("彼", tokens.get(0).value());
        assertEquals("は", tokens.get(1).value());
        assertEquals("Java", tokens.get(2).value());
        assertEquals("プログラマー", tokens.get(3).value());
        assertEquals("です", tokens.get(4).value());
        assertEquals("。", tokens.get(5).value());
    }

    @Test
    void tokenizeShouldHandleMultiplePunctuation() {
        List<Token> tokens = model.tokenize("えっ！？本当ですか？");

        assertEquals(7, tokens.size());
        assertEquals("えっ", tokens.get(0).value());
        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals("！", tokens.get(1).value());
        assertEquals(TokenType.Punctuation, tokens.get(2).type());
        assertEquals("？", tokens.get(2).value());
        assertEquals("本当", tokens.get(3).value());
        assertEquals("です", tokens.get(4).value());
        assertEquals("か", tokens.get(5).value());
        assertEquals("？", tokens.get(6).value());
    }

    @Test
    void areEquivalentShouldHandleBasicEquivalence() {
        assertTrue(model.areEquivalent("私は猫です", "私は猫です"));
        assertFalse(model.areEquivalent("私は猫です", "私は犬です"));
    }

    @Test
    void areEquivalentShouldIgnorePunctuation() {
        assertTrue(model.areEquivalent("こんにちは。", "こんにちは"));
        assertTrue(model.areEquivalent("こんにちは！", "こんにちは？"));
        assertTrue(model.areEquivalent("本当ですか？", "本当ですか！？"));
    }

    @Test
    void areEquivalentShouldIgnoreWhitespace() {
        assertTrue(model.areEquivalent("こんにちは", "  こんにちは  "));
        assertTrue(model.areEquivalent("私 は 猫", "私は猫"));
    }

    @Test
    void tokenizeShouldHandleCompoundWords() {
        List<Token> tokens = model.tokenize("東京都新宿区");

        assertEquals(1, tokens.size());
        assertEquals("東京都新宿区", tokens.get(0).value());
    }

    @Test
    void tokenizeShouldHandleKatakana() {
        List<Token> tokens = model.tokenize("コンピューターを使います。");

        assertEquals(5, tokens.size());
        assertEquals("コンピューター", tokens.get(0).value());
        assertEquals("を", tokens.get(1).value());
        assertEquals("使い", tokens.get(2).value());
        assertEquals("ます", tokens.get(3).value());
        assertEquals("。", tokens.get(4).value());
    }
}
