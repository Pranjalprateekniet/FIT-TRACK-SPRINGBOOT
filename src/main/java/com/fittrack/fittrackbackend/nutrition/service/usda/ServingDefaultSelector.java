package com.fittrack.fittrackbackend.nutrition.service.usda;

import com.fittrack.fittrackbackend.nutrition.dto.ServingOptionResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServingDefaultSelector {

    public void selectDefault(List<ServingOptionResponse> servings, String baseFoodName) {
        if (servings == null || servings.isEmpty()) return;

        ServingOptionResponse best = null;
        int maxScore = -1;

        for (ServingOptionResponse serving : servings) {
            serving.setIsDefault(false); // Reset all
            int score = calculateScore(serving.getDisplayName(), baseFoodName);
            if (score > maxScore) {
                maxScore = score;
                best = serving;
            }
        }

        if (best != null) {
            best.setIsDefault(true);
        } else {
            // Fallback: first serving if all scored -1 (shouldn't happen since minimum is 0)
            servings.get(0).setIsDefault(true);
        }
    }

    private int calculateScore(String label, String baseFoodName) {
        if (label == null) return 0;
        String lower = label.toLowerCase();
        String baseLower = baseFoodName != null ? baseFoodName.toLowerCase() : "";

        if (lower.contains("medium")) return 100;
        if (lower.contains("large")) return 90;
        if (lower.contains("small")) return 80;

        // Whole food match: e.g. label is "1 banana" and base is "banana"
        if (!baseLower.isEmpty() && (lower.equals("1 " + baseLower) || lower.equals(baseLower))) {
            return 70;
        }

        if (lower.contains("piece")) return 65;
        if (lower.contains("cup")) return 60;
        if (lower.contains("glass")) return 55;
        if (lower.contains("bowl")) return 50;
        if (lower.contains("slice")) return 45;
        if (lower.contains("100 g") || lower.contains("100g")) return 30;

        return 0;
    }
}
