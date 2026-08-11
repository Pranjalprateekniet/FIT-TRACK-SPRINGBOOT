package com.fittrack.fittrackbackend.nutrition.service.usda;

import com.fittrack.fittrackbackend.nutrition.client.UsdaFoodResult;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SearchResultRanker {

    private static final List<String> DESSERT_KEYWORDS = List.of(
            "cake", "chips", "pie", "pudding", "split", "candy", "dessert", "milkshake", "ice cream"
    );

    private static final List<String> DRINK_KEYWORDS = List.of(
            "juice", "nectar", "drink", "smoothie"
    );

    public List<UsdaFoodResult> rank(List<UsdaFoodResult> results, String query) {
        return results.stream()
                .sorted(Comparator.comparingInt((UsdaFoodResult r) -> getScore(r, query)).reversed() // descending
                        .thenComparing(r -> r.dto().getFoodName().length()) // tie-breaker: shorter names
                        .thenComparing(r -> r.dto().getFoodName().toLowerCase().contains("raw") ? 0 : 1) // tie-breaker: raw foods
                        .thenComparing(r -> r.dto().getFoodName()) // alphabetic fallback
                )
                .collect(Collectors.toList());
    }

    private int getScore(UsdaFoodResult result, String query) {
        String foodName = result.dto().getFoodName().toLowerCase();
        String dataType = result.dto().getDataType();
        String qLower = query.toLowerCase();

        int score = 0;

        if (foodName.equals(qLower)) {
            score += 100;
        } else if (foodName.startsWith(qLower + " ") || foodName.startsWith(qLower + ",")) {
            score += 70;
        } else if (foodName.matches(".*\\b" + qLower + "\\b.*")) {
            score += 50; // Whole-word match
        }

        if (foodName.contains("raw")) {
            score += 40;
        }

        if ("Foundation".equalsIgnoreCase(dataType)) {
            score += 30;
        } else if ("SR Legacy".equalsIgnoreCase(dataType)) {
            score += 30;
        } else if ("Survey".equalsIgnoreCase(dataType) || "Survey (FNDDS)".equalsIgnoreCase(dataType)) {
            score += 20;
        } else if ("Branded".equalsIgnoreCase(dataType)) {
            score -= 20;
        }

        for (String keyword : DESSERT_KEYWORDS) {
            if (foodName.matches(".*\\b" + keyword + "\\b.*")) {
                score -= 40;
                break;
            }
        }

        for (String keyword : DRINK_KEYWORDS) {
            if (foodName.matches(".*\\b" + keyword + "\\b.*")) {
                score -= 30;
                break;
            }
        }

        return score;
    }
}
