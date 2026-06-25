package com.fittrack.fittrackbackend.nutrition.service;

import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import com.fittrack.fittrackbackend.nutrition.repository.FoodNutritionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class FoodNutritionService {

    private final FoodNutritionRepository foodNutritionRepository;
    private final FuzzySearchService fuzzySearchService;

    public List<FoodNutrition> searchFoods(String query) {

        if (query == null || query.isBlank()) {
            return List.of();
        }

        query = query.trim().toLowerCase();

        // 1. Exact Match
        Optional<FoodNutrition> exactMatch =
                foodNutritionRepository
                        .findByFoodNameIgnoreCase(query);

        if (exactMatch.isPresent()) {
            return List.of(exactMatch.get());
        }

        // 2. Partial Match
        List<FoodNutrition> partialMatches =
                foodNutritionRepository
                        .findTop20ByFoodNameContainingIgnoreCase(query);

        if (!partialMatches.isEmpty()) {
            return partialMatches;
        }

        // 3. Fuzzy Match
        FoodNutrition closestMatch =
                fuzzySearchService.findClosestMatch(
                        query,
                        foodNutritionRepository.findAll()
                );

        if (closestMatch != null) {
            return List.of(closestMatch);
        }

        return List.of();
    }
}
