package com.fittrack.fittrackbackend.nutrition.service;

import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import com.fittrack.fittrackbackend.nutrition.repository.FoodNutritionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodNutritionService {
    private final FoodNutritionRepository foodNutritionRepository;
        public List<FoodNutrition>searchFoods(String query){
            return foodNutritionRepository.findTop20ByFoodNameContainingIgnoreCase(query);

    }
}
