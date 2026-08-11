package com.fittrack.fittrackbackend.nutrition.controller;
import com.fittrack.fittrackbackend.nutrition.client.USDAClient;
import com.fittrack.fittrackbackend.nutrition.dto.FoodSearchResponse;
import com.fittrack.fittrackbackend.nutrition.service.FoodNutritionService;
import com.fittrack.fittrackbackend.nutrition.search.FoodSearchRankingService;
import com.fittrack.fittrackbackend.nutrition.search.RankingDebugger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nutrition")
public class FoodNutritionController {

    private final FoodNutritionService foodNutritionService;
    private final USDAClient usdaClient;
    private final FoodSearchRankingService newSearchRankingService;
    private final RankingDebugger rankingDebugger;

    /**
     * Unified food search.
     * Returns local-DB foods (with serving presets) and USDA fallback results
     * in a single {@link FoodSearchResponse} list.
     */
    @GetMapping("/search")
    public ResponseEntity<List<FoodSearchResponse>> searchFoods(
            @RequestParam String query) {

        return ResponseEntity.ok(
                foodNutritionService.searchFoodsAsDto(query)
        );
    }

    @GetMapping("/usda")
    public ResponseEntity<List<FoodSearchResponse>> searchUSDA(
            @RequestParam String query) {

        return ResponseEntity.ok(
                usdaClient.searchFood(query)
                          .stream()
                          .map(com.fittrack.fittrackbackend.nutrition.client.UsdaFoodResult::dto)
                          .toList()
        );
    }

    @GetMapping("/search/v2")
    public ResponseEntity<List<FoodSearchResponse>> searchFoodsV2(
            @RequestParam String query) {
        return ResponseEntity.ok(newSearchRankingService.search(query));
    }

    @GetMapping("/search/debug")
    public ResponseEntity<RankingDebugger.DebugResponse> searchDebug(
            @RequestParam String query) {
        return ResponseEntity.ok(rankingDebugger.debug(query));
    }
}