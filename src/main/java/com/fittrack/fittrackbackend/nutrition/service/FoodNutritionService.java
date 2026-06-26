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
            return Collections.emptyList();
        }

        query = query.trim();
        final String searchQuery = query.toLowerCase();

        // 1. Exact Match
        Optional<FoodNutrition> exactMatch =
                foodNutritionRepository.findByFoodNameIgnoreCase(query);

        if (exactMatch.isPresent()) {
            return List.of(exactMatch.get());
        }

        List<FoodNutrition> result = new ArrayList<>();

        // 2. Foods starting with query
        List<FoodNutrition> startsWithMatches =
                foodNutritionRepository
                        .findTop10ByFoodNameStartingWithIgnoreCase(query);

        result.addAll(startsWithMatches);

        // 3. Foods where any word matches the query
        List<FoodNutrition> wordMatches =
                foodNutritionRepository.searchByWord(query);

        for (FoodNutrition food : wordMatches) {

            boolean alreadyPresent =
                    result.stream()
                            .anyMatch(
                                    f -> f.getId().equals(food.getId())
                            );

            if (!alreadyPresent) {
                result.add(food);
            }
        }

        // 4. Contains search
        List<FoodNutrition> containsMatches =
                foodNutritionRepository
                        .findTop10ByFoodNameContainingIgnoreCase(query);

        for (FoodNutrition food : containsMatches) {

            boolean alreadyPresent =
                    result.stream()
                            .anyMatch(
                                    f -> f.getId().equals(food.getId())
                            );

            if (!alreadyPresent) {
                result.add(food);
            }
        }

        // 5. Fuzzy search only if nothing found
        if (result.isEmpty() && query.length() >= 4) {

            FoodNutrition closestMatch =
                    fuzzySearchService.findClosestMatch(
                            query,
                            foodNutritionRepository.findAll()
                    );

            if (closestMatch != null) {
                result.add(closestMatch);
            }
        }

        // Don't call USDA for every keystroke
        if (query.length() < 3) {

            sortResults(result, searchQuery);
            return removeDuplicates(result);
        }

        // 6. USDA fallback only if local DB has very few results
        if (result.size() < 5) {

            List<FoodSearchResponse> usdaFoods =
                    usdaClient.searchFood(query);

            Set<String> processedFoods = new HashSet<>();

            for (FoodSearchResponse dto : usdaFoods) {

                if (dto.getFoodName() == null ||
                        dto.getFoodName().isBlank()) {
                    continue;
                }

                String normalizedName =
                        dto.getFoodName().trim().toLowerCase();

                if (!processedFoods.add(normalizedName)) {
                    continue;
                }

                Optional<FoodNutrition> existing =
                        foodNutritionRepository
                                .findByFoodNameIgnoreCase(normalizedName);

                if (existing.isPresent()) {

                    boolean alreadyPresent =
                            result.stream()
                                    .anyMatch(
                                            f -> f.getId()
                                                    .equals(existing.get().getId())
                                    );

                    if (!alreadyPresent) {
                        result.add(existing.get());
                    }

                } else {

                    FoodNutrition saved =
                            foodNutritionRepository.save(
                                    mapToEntity(dto)
                            );

                    result.add(saved);
                }
            }
        }

        for (FoodNutrition food : result) {
            System.out.println(
                    food.getFoodName()
                            + " -> "
                            + getMatchScore(
                            food.getFoodName().toLowerCase(),
                            searchQuery
                    )
            );
        }

        sortResults(result, searchQuery);


        return removeDuplicates(result);
    }

    private void sortResults(List<FoodNutrition> result,
                             String searchQuery) {

        result.sort(
                Comparator
                        .comparingInt((FoodNutrition food) ->
                                getMatchScore(
                                        food.getFoodName().toLowerCase(),
                                        searchQuery))
                        .thenComparing(food ->
                                food.getFoodName().length())
        );
    }

    private int getMatchScore(String foodName, String query) {

        // Exact full match
        if (foodName.equals(query)) {
            return 0;
        }

        // Starts with query: "Egg Curry", "Egg Raw"
        if (foodName.startsWith(query + " ")) {
            return 1;
        }

        // Second word is query: "Boiled Egg Whites"
        String[] words = foodName.split("\\s+");

        if (words.length > 1 && words[1].equals(query)) {
            return 2;
        }

        // Contains exact word anywhere
        for (String word : words) {
            if (word.equals(query)) {
                return 3;
            }
        }

        // Prefix match inside a word (eggplant)
        for (String word : words) {
            if (word.startsWith(query)) {
                return 4;
            }
        }

        return 5;
    }

    private List<FoodNutrition> removeDuplicates(
            List<FoodNutrition> foods) {

        Map<Long, FoodNutrition> uniqueFoods =
                new LinkedHashMap<>();

        for (FoodNutrition food : foods) {
            uniqueFoods.putIfAbsent(food.getId(), food);
        }

        return new ArrayList<>(uniqueFoods.values());
    }

    private FoodNutrition mapToEntity(FoodSearchResponse dto) {

        return FoodNutrition.builder()
                .foodName(dto.getFoodName().trim())
                .calories(
                        dto.getCalories() != null
                                ? dto.getCalories().intValue()
                                : 0
                )
                .proteinG(
                        dto.getProtein() != null
                                ? dto.getProtein()
                                : 0.0
                )
                .carbsG(
                        dto.getCarbs() != null
                                ? dto.getCarbs()
                                : 0.0
                )
                .fatG(
                        dto.getFat() != null
                                ? dto.getFat()
                                : 0.0
                )
                .fiberG(
                        dto.getFiber() != null
                                ? dto.getFiber()
                                : 0.0
                )
                .cholesterolMg(
                        dto.getCholesterol() != null
                                ? dto.getCholesterol()
                                : 0.0
                )
                .freeSugarG(
                        dto.getFreeSugar() != null
                                ? dto.getFreeSugar()
                                : 0.0
                )
                .servingSizeG(
                        dto.getServingSizeG() != null
                                ? dto.getServingSizeG()
                                : 100.0
                )
                .source(
                        "USDA_" +
                                dto.getDataType()
                                        .toUpperCase()
                                        .replace(" ", "_")
                )
                .build();
    }
}