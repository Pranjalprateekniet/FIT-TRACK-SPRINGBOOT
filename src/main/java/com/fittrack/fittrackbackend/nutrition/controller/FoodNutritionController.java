package com.fittrack.fittrackbackend.nutrition.controller;

import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import com.fittrack.fittrackbackend.nutrition.service.FoodNutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FoodNutritionController {
    private final FoodNutritionService foodNutritionService;

    @GetMapping("/api/food/search")
    public List<FoodNutrition>searchFoods(@RequestParam String q){
        return foodNutritionService.searchFoods(q);
    }

}
