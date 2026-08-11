package com.fittrack.fittrackbackend.nutrition.search;

import com.fittrack.fittrackbackend.nutrition.entity.FoodSearchMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class MatchTierClassifierTest {

    private MatchTierClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new MatchTierClassifier();
    }

    private FoodSearchMetadata makeCandidate(String tokens) {
        FoodSearchMetadata metadata = new FoodSearchMetadata();
        metadata.setNormalizedTokens(tokens);
        return metadata;
    }

    @Test
    void testTier0ExactMatch() {
        NormalizedQuery nq = new NormalizedQuery("egg", List.of("egg"), 1);
        FoodSearchMetadata candidate = makeCandidate("egg");
        assertEquals(0, classifier.classify(nq, candidate));
    }

    @Test
    void testTier1Prefix() {
        NormalizedQuery nq = new NormalizedQuery("egg", List.of("egg"), 1);
        FoodSearchMetadata candidate = makeCandidate("egg whole");
        assertEquals(1, classifier.classify(nq, candidate));
    }

    @Test
    void testTier2AllOrdered() {
        NormalizedQuery nq = new NormalizedQuery("egg white", List.of("egg", "white"), 2);
        FoodSearchMetadata candidate = makeCandidate("raw egg white");
        assertEquals(2, classifier.classify(nq, candidate));
    }

    @Test
    void testTier3AllUnordered() {
        NormalizedQuery nq = new NormalizedQuery("white egg", List.of("white", "egg"), 2);
        FoodSearchMetadata candidate = makeCandidate("egg white raw");
        assertEquals(3, classifier.classify(nq, candidate));
    }

    @Test
    void testTier4Partial() {
        NormalizedQuery nq = new NormalizedQuery("egg white raw", List.of("egg", "white", "raw"), 3);
        FoodSearchMetadata candidate = makeCandidate("egg raw"); // 2 out of 3 matches (66%)
        assertEquals(4, classifier.classify(nq, candidate));
    }
}
