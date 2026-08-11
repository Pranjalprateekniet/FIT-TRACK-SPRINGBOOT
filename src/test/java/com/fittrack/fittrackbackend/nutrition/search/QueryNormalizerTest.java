package com.fittrack.fittrackbackend.nutrition.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryNormalizerTest {

    private QueryNormalizer normalizer;
    private QueryVocabulary vocab;

    @BeforeEach
    void setUp() {
        vocab = new QueryVocabulary(
                Set.of("egg", "egg white", "chicken", "chicken breast", "banana", "rice"),
                Set.of("raw"),
                Set.of(),
                Set.of()
        );
        normalizer = new QueryNormalizer(vocab);
    }

    @Test
    void testEggWhite() {
        NormalizedQuery nq = normalizer.normalize("egg white");
        assertEquals("egg white", nq.normalizedText());
        assertEquals(List.of("egg", "white"), nq.tokens());
    }

    @Test
    void testEggYolk() {
        NormalizedQuery nq = normalizer.normalize("egg yolk");
        assertEquals("egg yolk", nq.normalizedText());
        assertEquals(List.of("egg", "yolk"), nq.tokens());
    }

    @Test
    void testChickenBreast() {
        NormalizedQuery nq = normalizer.normalize("chicken breast");
        assertEquals("chicken breast", nq.normalizedText());
        assertEquals(List.of("chicken", "breast"), nq.tokens());
    }

    @Test
    void testBananaRaw() {
        NormalizedQuery nq = normalizer.normalize("banana raw");
        assertEquals("banana raw", nq.normalizedText());
        assertEquals(List.of("banana", "raw"), nq.tokens());
    }

    @Test
    void testChickenBreastRaw() {
        NormalizedQuery nq = normalizer.normalize("chicken breast raw");
        assertEquals("chicken breast raw", nq.normalizedText());
        assertEquals(List.of("chicken", "breast", "raw"), nq.tokens());
    }

    @Test
    void testEggWhiteWithNbsp() {
        // Simulates the exact bug where Unicode NBSP was swallowed
        NormalizedQuery nq = normalizer.normalize("egg\u00A0white");
        assertEquals("egg white", nq.normalizedText());
        assertEquals(List.of("egg", "white"), nq.tokens());
    }
}
