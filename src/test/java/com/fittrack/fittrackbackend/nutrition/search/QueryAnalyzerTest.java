package com.fittrack.fittrackbackend.nutrition.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Set;

class QueryAnalyzerTest {

    private QueryAnalyzer analyzer;
    private QueryVocabulary vocab;

    @BeforeEach
    void setUp() {
        vocab = new QueryVocabulary(
                Set.of("egg", "egg white", "chicken", "chicken breast", "banana", "rice"),
                Set.of("raw", "boiled", "grilled"),
                Set.of("sandwich", "bread", "salad"),
                Set.of("mcdonalds")
        );

        analyzer = new QueryAnalyzer(vocab);
    }

    @Test
    void testEggWhite() {
        NormalizedQuery nq = new NormalizedQuery("egg white", List.of("egg", "white"), 2);
        QueryAnalysis qa = analyzer.analyze(nq);

        assertEquals(QueryIntent.SUBTYPE, qa.intent());
        assertTrue(qa.foodIdentityTerms().contains("egg white"));
        assertTrue(qa.modifierTerms().isEmpty());
        assertTrue(qa.preparationTerms().isEmpty());
        assertTrue(qa.compoundTerms().isEmpty());
    }

    @Test
    void testGrilledChickenSandwich() {
        NormalizedQuery nq = new NormalizedQuery("grilled chicken sandwich", List.of("grilled", "chicken", "sandwich"), 3);
        QueryAnalysis qa = analyzer.analyze(nq);

        assertEquals(QueryIntent.COMPOUND_RECIPE, qa.intent());
        assertTrue(qa.preparationTerms().contains("grilled"));
        assertTrue(qa.foodIdentityTerms().contains("chicken"));
        assertTrue(qa.compoundTerms().contains("sandwich"));
        assertTrue(qa.modifierTerms().isEmpty());
    }

    @Test
    void testMcdonaldsEgg() {
        NormalizedQuery nq = new NormalizedQuery("mcdonalds egg", List.of("mcdonalds", "egg"), 2);
        QueryAnalysis qa = analyzer.analyze(nq);

        assertEquals(QueryIntent.BRAND, qa.intent());
        assertTrue(qa.brandTerms().contains("mcdonalds"));
        assertTrue(qa.foodIdentityTerms().contains("egg"));
        assertTrue(qa.modifierTerms().isEmpty());
    }

    @Test
    void testBroadBaseFoodIntent() {
        NormalizedQuery nq = new NormalizedQuery("banana", List.of("banana"), 1);
        QueryAnalysis qa = analyzer.analyze(nq);

        assertEquals(QueryIntent.BROAD_BASE_FOOD, qa.intent());
        assertTrue(qa.isBroadGeneric());
    }

    @Test
    void testPreparationIntent() {
        NormalizedQuery nq = new NormalizedQuery("boiled egg", List.of("boiled", "egg"), 2);
        QueryAnalysis qa = analyzer.analyze(nq);

        assertEquals(QueryIntent.PREPARATION, qa.intent());
    }

    @Test
    void testTypoRecoveryAddsBaseFoodIntent() {
        NormalizedQuery nq = new NormalizedQuery("bananna", List.of("bananna"), 1);
        QueryAnalysis qa = analyzer.analyze(nq);

        assertEquals(QueryIntent.BROAD_BASE_FOOD, qa.intent());
        assertTrue(qa.foodIdentityTerms().contains("banana"));
        assertTrue(qa.isBroadGeneric());
    }
}
