package com.fittrack.fittrackbackend.nutrition.service;

import com.fittrack.fittrackbackend.nutrition.client.USDAClient;
import com.fittrack.fittrackbackend.nutrition.client.UsdaFoodResult;
import com.fittrack.fittrackbackend.nutrition.dto.FoodSearchResponse;
import com.fittrack.fittrackbackend.nutrition.dto.ServingOptionResponse;
import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import com.fittrack.fittrackbackend.nutrition.entity.FoodServing;
import com.fittrack.fittrackbackend.nutrition.repository.FoodNutritionRepository;
import com.fittrack.fittrackbackend.nutrition.repository.FoodServingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FoodNutritionService {

    private final FoodNutritionRepository foodNutritionRepository;
    private final FoodServingRepository foodServingRepository;
    private final FuzzySearchService fuzzySearchService;
    private final USDAClient usdaClient;

    /**
     * Legacy search entry point.
     */
    public List<FoodNutrition> searchFoods(String query) {
        return searchFoodsInternal(query);
    }

    private void sortResults(
            List<FoodNutrition> result,
            String searchQuery) {

        result.sort(
                Comparator
                        .comparingInt((FoodNutrition food) -> getMatchScore(
                                food.getFoodName().toLowerCase(Locale.ROOT),
                                searchQuery))
                        .thenComparing(food -> food.getFoodName().length()));
    }

    private int getMatchScore(String foodName, String query) {

        // Exact full match
        if (foodName.equals(query)) {
            return 0;
        }

        // Starts with query
        // Example: "egg raw", "egg curry"
        if (foodName.startsWith(query + " ")) {
            return 1;
        }

        String[] words = foodName.split("\\s+");

        // Query is the second word
        // Example: "boiled egg whites"
        if (words.length > 1 && words[1].equals(query)) {
            return 2;
        }

        // Exact word match anywhere
        for (String word : words) {
            if (word.equals(query)) {
                return 3;
            }
        }

        // Prefix match inside a word
        // Example: "egg" -> "eggplant"
        for (String word : words) {
            if (word.startsWith(query)) {
                return 4;
            }
        }

        return 5;
    }

    private List<FoodNutrition> removeDuplicates(
            List<FoodNutrition> foods) {

        Map<Long, FoodNutrition> uniqueFoods = new LinkedHashMap<>();

        for (FoodNutrition food : foods) {
            uniqueFoods.putIfAbsent(food.getId(), food);
        }

        return new ArrayList<>(uniqueFoods.values());
    }

    private FoodNutrition mapToEntity(FoodSearchResponse dto) {

        String dataType = dto.getDataType() != null
                ? dto.getDataType()
                : "UNKNOWN";

        return FoodNutrition.builder()
                .foodName(
                        dto.getFoodName() != null
                                ? dto.getFoodName().trim()
                                : "")
                .calories(
                        dto.getCalories() != null
                                ? dto.getCalories().intValue()
                                : 0)
                .proteinG(
                        dto.getProtein() != null
                                ? dto.getProtein()
                                : 0.0)
                .carbsG(
                        dto.getCarbs() != null
                                ? dto.getCarbs()
                                : 0.0)
                .fatG(
                        dto.getFat() != null
                                ? dto.getFat()
                                : 0.0)
                .fiberG(
                        dto.getFiber() != null
                                ? dto.getFiber()
                                : 0.0)
                .cholesterolMg(
                        dto.getCholesterol() != null
                                ? dto.getCholesterol()
                                : 0.0)
                .freeSugarG(
                        dto.getFreeSugar() != null
                                ? dto.getFreeSugar()
                                : 0.0)
                .servingSizeG(
                        dto.getServingSizeG() != null
                                ? dto.getServingSizeG()
                                : 100.0)
                .source(
                        "USDA_" +
                                dataType
                                        .toUpperCase(Locale.ROOT)
                                        .replace(" ", "_"))
                .build();
    }

    /**
     * Full local + USDA search pipeline returning DTOs with serving options.
     */
    public List<FoodSearchResponse> searchFoodsAsDto(String query) {

        List<FoodNutrition> foods = searchFoodsInternal(query);

        List<FoodSearchResponse> result = new ArrayList<>();

        for (FoodNutrition food : foods) {
            result.add(toDto(food));
        }

        return result;
    }

    /**
     * Core search pipeline.
     *
     * Order:
     * 1. Exact match
     * 2. Starts-with matches
     * 3. Word matches
     * 4. Contains matches
     * 5. Fuzzy fallback
     * 6. USDA fallback when local results are insufficient
     */
    private List<FoodNutrition> searchFoodsInternal(String query) {

        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        query = query.trim();

        final String searchQuery = query.toLowerCase(Locale.ROOT);

        // --------------------------------------------------------------
        // 1. Exact match
        // --------------------------------------------------------------

        Optional<FoodNutrition> exactMatch = foodNutritionRepository
                .findByFoodNameIgnoreCase(query);

        if (exactMatch.isPresent()) {
            return List.of(exactMatch.get());
        }

        List<FoodNutrition> result = new ArrayList<>();

        // --------------------------------------------------------------
        // 2. Foods starting with query
        // --------------------------------------------------------------

        List<FoodNutrition> startsWithMatches = foodNutritionRepository
                .findTop10ByFoodNameStartingWithIgnoreCase(query);

        result.addAll(startsWithMatches);

        // --------------------------------------------------------------
        // 3. Foods where any word matches the query
        // --------------------------------------------------------------

        List<FoodNutrition> wordMatches = foodNutritionRepository.searchByWord(query);

        for (FoodNutrition food : wordMatches) {

            boolean alreadyPresent = result.stream()
                    .anyMatch(
                            f -> f.getId().equals(food.getId()));

            if (!alreadyPresent) {
                result.add(food);
            }
        }

        // --------------------------------------------------------------
        // 4. Contains search
        // --------------------------------------------------------------

        List<FoodNutrition> containsMatches = foodNutritionRepository
                .findTop10ByFoodNameContainingIgnoreCase(query);

        for (FoodNutrition food : containsMatches) {

            boolean alreadyPresent = result.stream()
                    .anyMatch(
                            f -> f.getId().equals(food.getId()));

            if (!alreadyPresent) {
                result.add(food);
            }
        }

        // --------------------------------------------------------------
        // 5. Fuzzy fallback
        // --------------------------------------------------------------

        if (result.isEmpty() && query.length() >= 4) {

            FoodNutrition closestMatch = fuzzySearchService.findClosestMatch(
                    query,
                    foodNutritionRepository.findAll());

            if (closestMatch != null) {
                result.add(closestMatch);
            }
        }

        // --------------------------------------------------------------
        // Queries shorter than 3 characters do not hit USDA
        // --------------------------------------------------------------

        if (query.length() < 3) {

            sortResults(result, searchQuery);

            return removeDuplicates(result);
        }

        // --------------------------------------------------------------
        // 6. USDA fallback
        // --------------------------------------------------------------

        if (result.size() < 5) {

            List<UsdaFoodResult> usdaFoods = usdaClient.searchFood(query);

            Set<String> processedFoods = new HashSet<>();

            for (UsdaFoodResult usdaResult : usdaFoods) {

                if (usdaResult == null || usdaResult.dto() == null) {
                    continue;
                }

                FoodSearchResponse dto = usdaResult.dto();

                if (dto.getFoodName() == null ||
                        dto.getFoodName().isBlank()) {
                    continue;
                }

                String normalizedName = dto.getFoodName()
                        .trim()
                        .toLowerCase(Locale.ROOT);

                if (!processedFoods.add(normalizedName)) {
                    continue;
                }

                Optional<FoodNutrition> existing = foodNutritionRepository
                        .findByFoodNameIgnoreCase(
                                dto.getFoodName().trim());

                if (existing.isPresent()) {

                    FoodNutrition existingFood = existing.get();

                    boolean alreadyPresent = result.stream()
                            .anyMatch(
                                    f -> f.getId()
                                            .equals(existingFood.getId()));

                    if (!alreadyPresent) {
                        result.add(existingFood);
                    }

                } else {

                    FoodNutrition newFood = mapToEntity(dto);

                    newFood.setFdcId(
                            usdaResult.fdcId());

                    FoodNutrition saved = foodNutritionRepository.save(
                            newFood);

                    result.add(saved);
                }
            }
        }

        // --------------------------------------------------------------
        // Final ranking + duplicate removal
        // --------------------------------------------------------------

        sortResults(result, searchQuery);

        return removeDuplicates(result);
    }

    /**
     * Converts FoodNutrition entity into the API DTO and attaches
     * serving options.
     *
     * Priority:
     *
     * 1. Local FoodServing records
     * 2. USDA serving data using FDC ID
     * 3. Empty list if serving data is unavailable
     */
    private FoodSearchResponse toDto(FoodNutrition food) {

        List<FoodServing> dbServings = foodServingRepository
                .findByFoodIdOrderByDisplayOrderAsc(
                        food.getId());

        List<ServingOptionResponse> servingDtos;

        // --------------------------------------------------------------
        // Case 1: Local database servings
        // --------------------------------------------------------------

        if (!dbServings.isEmpty()) {

            servingDtos = new ArrayList<>();

            for (FoodServing serving : dbServings) {

                servingDtos.add(
                        new ServingOptionResponse(
                                serving.getId(),
                                serving.getDisplayName(),
                                serving.getServingWeightGrams(),
                                serving.getIsDefault()));
            }

        }

        // --------------------------------------------------------------
        // Case 2: USDA food with FDC ID
        // --------------------------------------------------------------

        else if (food.getFdcId() != null) {

            servingDtos = usdaClient.fetchServingsForFdcId(
                    food.getFdcId(),
                    food.getFoodName());

        }

        // --------------------------------------------------------------
        // Case 3: USDA food without FDC ID
        // --------------------------------------------------------------

        else if (food.isUsdaFood()) {

            servingDtos = new ArrayList<>();

        }

        // --------------------------------------------------------------
        // Case 4: Local food without serving presets
        // --------------------------------------------------------------

        else {

            servingDtos = new ArrayList<>();
        }

        // --------------------------------------------------------------
        // Build DTO
        // --------------------------------------------------------------

        FoodSearchResponse dto = new FoodSearchResponse();

        dto.setId(food.getId());

        dto.setFoodName(
                food.getFoodName());

        dto.setCalories(
                food.getCalories() != null
                        ? food.getCalories().doubleValue()
                        : 0.0);

        dto.setProtein(
                food.getProteinG() != null
                        ? food.getProteinG()
                        : 0.0);

        dto.setCarbs(
                food.getCarbsG() != null
                        ? food.getCarbsG()
                        : 0.0);

        dto.setFat(
                food.getFatG() != null
                        ? food.getFatG()
                        : 0.0);

        dto.setFiber(
                food.getFiberG() != null
                        ? food.getFiberG()
                        : 0.0);

        dto.setCholesterol(
                food.getCholesterolMg() != null
                        ? food.getCholesterolMg()
                        : 0.0);

        dto.setFreeSugar(
                food.getFreeSugarG() != null
                        ? food.getFreeSugarG()
                        : 0.0);

        dto.setServingSizeG(
                food.getServingSizeG() != null
                        ? food.getServingSizeG()
                        : 100.0);

        dto.setDataType(
                food.isLocalFood()
                        ? "LOCAL"
                        : food.getSource());

        dto.setServings(servingDtos);

        return dto;
    }
}