package com.fittrack.fittrackbackend.nutrition.repository;

import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoodNutritionRepository
        extends JpaRepository<FoodNutrition, Long> {

    Optional<FoodNutrition>
    findByFoodNameIgnoreCase(String foodName);

    List<FoodNutrition>
    findByFoodNameContainingIgnoreCase(String query);

    List<FoodNutrition>
    findTop20ByFoodNameContainingIgnoreCase(String query);
}