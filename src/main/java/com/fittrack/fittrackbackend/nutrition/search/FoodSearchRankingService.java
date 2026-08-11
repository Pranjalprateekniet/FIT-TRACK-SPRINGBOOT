package com.fittrack.fittrackbackend.nutrition.search;

import com.fittrack.fittrackbackend.nutrition.dto.FoodSearchResponse;
import com.fittrack.fittrackbackend.nutrition.dto.ServingOptionResponse;
import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import com.fittrack.fittrackbackend.nutrition.entity.FoodSearchMetadata;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FoodSearchRankingService {

    private final QueryNormalizer queryNormalizer;
    private final QueryAnalyzer queryAnalyzer;
    private final SearchCandidateRetriever candidateRetriever;
    private final MatchTierClassifier tierClassifier;
    private final SemanticScorer semanticScorer;
    private final DuplicateSuppressor duplicateSuppressor;

    public FoodSearchRankingService(QueryNormalizer queryNormalizer,
                                    QueryAnalyzer queryAnalyzer,
                                    SearchCandidateRetriever candidateRetriever,
                                    MatchTierClassifier tierClassifier,
                                    SemanticScorer semanticScorer,
                                    DuplicateSuppressor duplicateSuppressor) {
        this.queryNormalizer = queryNormalizer;
        this.queryAnalyzer = queryAnalyzer;
        this.candidateRetriever = candidateRetriever;
        this.tierClassifier = tierClassifier;
        this.semanticScorer = semanticScorer;
        this.duplicateSuppressor = duplicateSuppressor;
    }

    public List<FoodSearchResponse> search(String rawQuery) {
        NormalizedQuery nq = queryNormalizer.normalize(rawQuery);
        QueryAnalysis qa = queryAnalyzer.analyze(nq);

        List<FoodSearchMetadata> candidates = candidateRetriever.retrieve(nq);

        for (FoodSearchMetadata c : candidates) {
            int tier = tierClassifier.classify(nq, c);
            double semanticScore = semanticScorer.score(qa, c, nq);

            c.setFinalTier(tier);

            double tierBaseScore = (5 - tier) * 2.000;
            c.setFinalScore(tierBaseScore + semanticScore);
        }

        candidates.sort((c1, c2) -> {
            // 1. Final Score (Descending)
            int cmp = Double.compare(c2.getFinalScore(), c1.getFinalScore());
            if (cmp != 0) return cmp;

            // 2. Source Quality Priority (Ascending, lower is better: 1 Foundation, 2 Legacy, 3 Survey)
            cmp = Integer.compare(c1.getFoodTypePriority(), c2.getFoodTypePriority());
            if (cmp != 0) return cmp;

            // 3. Database ID (Ascending, lower wins for canonical variants)
            cmp = Long.compare(c1.getFoodId(), c2.getFoodId());
            if (cmp != 0) return cmp;

            // 4. Name Simplicity Token Count (Ascending, shorter wins)
            cmp = Integer.compare(c1.getNameTokenCount(), c2.getNameTokenCount());
            if (cmp != 0) return cmp;

            // 5. String Length (Ascending, shorter wins)
            return Integer.compare(c1.getNameLengthChars(), c2.getNameLengthChars());
        });

        List<FoodSearchMetadata> suppressed = duplicateSuppressor.suppress(candidates);
        return suppressed.stream().map(this::toDto).collect(Collectors.toList());
    }

    private FoodSearchResponse toDto(FoodSearchMetadata metadata) {
        FoodNutrition fn = metadata.getFoodNutrition();
        FoodSearchResponse dto = new FoodSearchResponse();
        dto.setId(fn.getId());
        dto.setFoodName(fn.getFoodName());
        dto.setCalories(fn.getCalories() != null ? fn.getCalories().doubleValue() : null);
        dto.setProtein(fn.getProteinG());
        dto.setCarbs(fn.getCarbsG());
        dto.setFat(fn.getFatG());
        dto.setFiber(fn.getFiberG());
        dto.setCholesterol(fn.getCholesterolMg());
        dto.setFreeSugar(fn.getFreeSugarG());
        dto.setServingSizeG(fn.getServingSizeG());
        dto.setDataType(fn.getFoodType());

        List<ServingOptionResponse> servingDtos = fn.getServings().stream()
                .map(s -> new ServingOptionResponse(s.getId(), s.getDisplayName(), s.getServingWeightGrams(), s.getIsDefault()))
                .collect(Collectors.toList());
        dto.setServings(servingDtos);
        return dto;
    }

}
