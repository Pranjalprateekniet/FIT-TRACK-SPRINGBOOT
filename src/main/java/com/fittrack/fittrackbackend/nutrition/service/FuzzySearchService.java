package com.fittrack.fittrackbackend.nutrition.service;

import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FuzzySearchService {

    public FoodNutrition findClosestMatch(String query, List<FoodNutrition> foods){

        if (query == null || query.isBlank() || foods.isEmpty()) {
            return null;
        }

        LevenshteinDistance distance =
                new LevenshteinDistance();

        FoodNutrition bestMatch = null;
        int bestDistance = Integer.MAX_VALUE;

        query = query.trim().toLowerCase();

        for (FoodNutrition food : foods) {

            String foodName =
                    food.getFoodName()
                    .trim()
                    .toLowerCase();

            int currentDistance =
                    distance.apply(query, foodName);

            if (currentDistance < bestDistance) {
                bestDistance = currentDistance;
                bestMatch = food;
            }
        }

        if (bestDistance <= Math.max(2, query.length() / 4)) {
            return bestMatch;
        }

        return null;

    }
}
