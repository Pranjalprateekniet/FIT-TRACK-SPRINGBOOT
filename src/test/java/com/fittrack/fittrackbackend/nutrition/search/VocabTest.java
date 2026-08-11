package com.fittrack.fittrackbackend.nutrition.search;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class VocabTest {
    @Autowired
    private QueryVocabulary vocab;

    @Test
    void testOutput() {
        System.out.println("Has ground? " + vocab.getBaseFoodVocab().contains("ground"));
        System.out.println("Has chicken ground? " + vocab.getBaseFoodVocab().contains("chicken ground"));
    }
}
