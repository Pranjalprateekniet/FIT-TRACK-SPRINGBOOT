package com.fittrack.fittrackbackend.nutrition.service;

import com.fittrack.fittrackbackend.nutrition.client.USDAClient;
import com.fittrack.fittrackbackend.nutrition.dto.FoodSearchResponse;
import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import com.fittrack.fittrackbackend.nutrition.repository.FoodNutritionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FoodNutritionService {

    private final FoodNutritionRepository foodNutritionRepository;
    private final FuzzySearchService fuzzySearchService;
    private final USDAClient usdaClient;

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

// 4. USDA Fallback

        List<FoodSearchResponse> usdaFoods =
                usdaClient.searchFood(query); // or searchFoods(query)

        if (usdaFoods.isEmpty()) {
            // 3. Fuzzy Match
            FoodNutrition closestMatch =
                    fuzzySearchService.findClosestMatch(
                            query,
                            foodNutritionRepository.findAll()
                    );
            if (closestMatch != null) {
                return List.of(closestMatch);
            }
        }

        List<FoodNutrition> result = new ArrayList<>();

        Set<String> processedFoods = new HashSet<>();

        for (FoodSearchResponse dto : usdaFoods) {

            String foodName =
                    dto.getFoodName().trim().toLowerCase();

            if(processedFoods.contains(foodName)) {
                continue;
            }

            processedFoods.add(foodName);

            Optional<FoodNutrition> existing =
                    foodNutritionRepository
                            .findByFoodNameIgnoreCase(dto.getFoodName());

            if(existing.isPresent()) {
                result.add(existing.get());
                continue;
            }

            FoodNutrition saved =
                    foodNutritionRepository.save(
                            mapToEntity(dto));

            result.add(saved);
        }

        return result;
    }

    private FoodNutrition mapToEntity(FoodSearchResponse dto) {

        return FoodNutrition.builder()
                .foodName(dto.getFoodName())
                .calories(dto.getCalories().intValue())
                .proteinG(dto.getProtein())
                .carbsG(dto.getCarbs())
                .fatG(dto.getFat())
                .fiberG(dto.getFiber())
                .cholesterolMg(dto.getCholesterol())
                .freeSugarG(dto.getFreeSugar())
                .servingSizeG(dto.getServingSizeG())
                .source("USDA_"+dto.getDataType().toUpperCase().replace(" ","_"))
                .build();
    }
}
