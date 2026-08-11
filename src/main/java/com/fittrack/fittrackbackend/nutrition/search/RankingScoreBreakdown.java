package com.fittrack.fittrackbackend.nutrition.search;

import java.util.List;

public record RankingScoreBreakdown(
        double lexicalScore,
        double exactMatchScore,
        double tokenCoverageScore,
        double modifierMatchScore,
        double genericityCanonicalScore,
        double modifierPenalty,
        double finalFeatureScore,
        List<String> reasons
) {
}
