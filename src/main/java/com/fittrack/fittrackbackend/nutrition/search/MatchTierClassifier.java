package com.fittrack.fittrackbackend.nutrition.search;

import com.fittrack.fittrackbackend.nutrition.entity.FoodSearchMetadata;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MatchTierClassifier {

    public int classify(NormalizedQuery nq, FoodSearchMetadata candidate) {
        String queryText = nq.normalizedText();
        String candidateTokensStr = candidate.getNormalizedTokens();

        if (queryText.isBlank()) return 5;

        // TIER 0: Exact Match
        if (candidateTokensStr.equals(queryText)) {
            return 0;
        }

        // TIER 1: Phrase Prefix Match
        if (candidateTokensStr.startsWith(queryText + " ")) {
            return 1;
        }

        // Token checks
        List<String> qTokens = nq.tokens();
        List<String> cTokens = Arrays.asList(candidateTokensStr.split(" "));

        boolean allTokensPresent = true;
        boolean strictlyIncreasing = true;
        int lastIndex = -1;

        int matchCount = 0;
        for (String qt : qTokens) {
            int idx = cTokens.indexOf(qt);
            if (idx != -1) {
                matchCount++;
                if (idx <= lastIndex) {
                    strictlyIncreasing = false;
                }
                lastIndex = idx;
            } else {
                allTokensPresent = false;
            }
        }

        if (allTokensPresent) {
            // TIER 2: All Tokens, Ordered
            if (strictlyIncreasing) {
                return 2;
            }
            // TIER 3: All Tokens, Unordered
            return 3;
        }

        // TIER 4: Majority Token Match (>= 60%)
        int threshold = (int) Math.ceil(qTokens.size() * 0.60);
        if (matchCount >= threshold) {
            return 4;
        }

        // TIER 5: Fuzzy Fallback
        return 5;
    }
}
