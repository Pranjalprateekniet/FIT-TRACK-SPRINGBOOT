package com.fittrack.fittrackbackend.nutrition.search;

import com.fittrack.fittrackbackend.nutrition.entity.FoodSearchMetadata;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankingDebugger {

    private final QueryNormalizer queryNormalizer;
    private final QueryAnalyzer queryAnalyzer;
    private final SearchCandidateRetriever candidateRetriever;
    private final MatchTierClassifier tierClassifier;
    private final SemanticScorer semanticScorer;

    public RankingDebugger(QueryNormalizer queryNormalizer,
                           QueryAnalyzer queryAnalyzer,
                           SearchCandidateRetriever candidateRetriever,
                           MatchTierClassifier tierClassifier,
                           SemanticScorer semanticScorer) {
        this.queryNormalizer = queryNormalizer;
        this.queryAnalyzer = queryAnalyzer;
        this.candidateRetriever = candidateRetriever;
        this.tierClassifier = tierClassifier;
        this.semanticScorer = semanticScorer;
    }

    @Data
    public static class DebugResult {
        private String foodName;
        private Long foodId;
        private String normalizedTokens;
        private int matchTier;
        private double lexicalScore;
        private double exactMatchScore;
        private double tokenCoverage;
        private double modifierMatch;
        private double genericityCanonicalScore;
        private double modifierPenalty;
        private double semanticScore;
        private double relevanceScore;
        private double finalScore;
        private List<String> reasons;
    }

    @Data
    public static class DebugResponse {
        private NormalizedQuery normalizedQuery;
        private QueryAnalysis queryAnalysis;
        private List<DebugResult> results;
    }

    public DebugResponse debug(String rawQuery) {
        NormalizedQuery nq = queryNormalizer.normalize(rawQuery);
        QueryAnalysis qa = queryAnalyzer.analyze(nq);

        List<FoodSearchMetadata> candidates = candidateRetriever.retrieve(nq);

        for (FoodSearchMetadata c : candidates) {
            int tier = tierClassifier.classify(nq, c);
            double semanticScore = semanticScorer.score(qa, c, nq);

            c.setFinalTier(tier);
            c.setFinalScore(semanticScore);
        }

        candidates.sort((c1, c2) -> {
            int cmp = Double.compare(c2.getFinalScore(), c1.getFinalScore());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(c1.getFoodTypePriority(), c2.getFoodTypePriority());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(c1.getNameTokenCount(), c2.getNameTokenCount());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(c1.getNameLengthChars(), c2.getNameLengthChars());
            if (cmp != 0) return cmp;
            return Long.compare(c1.getFoodId(), c2.getFoodId());
        });

        List<DebugResult> debugResults = candidates.stream().limit(50).map(c -> {
            DebugResult r = new DebugResult();
            r.setFoodName(c.getFoodName());
            r.setFoodId(c.getFoodId());
            r.setNormalizedTokens(c.getNormalizedTokens());
            r.setMatchTier(c.getFinalTier());
            RankingScoreBreakdown scoreBreakdown = semanticScorer.explain(qa, c, nq);
            r.setLexicalScore(scoreBreakdown.lexicalScore());
            r.setExactMatchScore(scoreBreakdown.exactMatchScore());
            r.setTokenCoverage(scoreBreakdown.tokenCoverageScore());
            r.setModifierMatch(scoreBreakdown.modifierMatchScore());
            r.setGenericityCanonicalScore(scoreBreakdown.genericityCanonicalScore());
            r.setModifierPenalty(scoreBreakdown.modifierPenalty());
            r.setSemanticScore(scoreBreakdown.finalFeatureScore());
            r.setRelevanceScore(scoreBreakdown.finalFeatureScore());
            r.setFinalScore(c.getFinalScore());
            r.setReasons(scoreBreakdown.reasons());
            return r;
        }).collect(Collectors.toList());

        DebugResponse resp = new DebugResponse();
        resp.setNormalizedQuery(nq);
        resp.setQueryAnalysis(qa);
        resp.setResults(debugResults);
        return resp;
    }

}
