package com.fittrack.fittrackbackend.nutrition.search;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RankingDebuggerTest {

    @Autowired
    private RankingDebugger rankingDebugger;

    @Test
    void testDebugOutputs() {
        String[] queries = {"egg white", "egg yolk", "chicken breast", "banana raw"};
        for (String q : queries) {
            RankingDebugger.DebugResponse resp = rankingDebugger.debug(q);
            System.out.println("--- DEBUG OUTPUT FOR: " + q + " ---");
            System.out.println("normalizedText: " + resp.getNormalizedQuery().normalizedText());
            System.out.println("tokens: " + resp.getNormalizedQuery().tokens());
            System.out.println("foodIdentityTerms: " + resp.getQueryAnalysis().foodIdentityTerms());
            System.out.println("modifierTerms: " + resp.getQueryAnalysis().modifierTerms());
            System.out.println("------------------------------------");
        }
    }
}
