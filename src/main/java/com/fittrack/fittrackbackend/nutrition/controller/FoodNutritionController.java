package com.fittrack.fittrackbackend.nutrition.controller;

import com.fittrack.fittrackbackend.nutrition.client.USDAClient;
import com.fittrack.fittrackbackend.nutrition.dto.FoodSearchResponse;
import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import com.fittrack.fittrackbackend.nutrition.service.FoodNutritionService;
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

    @GetMapping("/search")
    public ResponseEntity<List<FoodNutrition>> searchFoods(
            @RequestParam String query) {

        return ResponseEntity.ok(
                foodNutritionService.searchFoods(query)
        );
    }

    @GetMapping("/usda")
    public ResponseEntity<List<FoodSearchResponse>> searchUSDA(
            @RequestParam String query) {

        return ResponseEntity.ok(
                usdaClient.searchFood(query)
        );
    }
}