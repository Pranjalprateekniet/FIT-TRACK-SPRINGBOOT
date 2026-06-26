package com.fittrack.fittrackbackend.nutrition.repository;

import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FoodNutritionRepository
        extends JpaRepository<FoodNutrition, Long> {

    Optional<FoodNutrition>
    findByFoodNameIgnoreCase(String foodName);

    List<FoodNutrition>
    findTop10ByFoodNameStartingWithIgnoreCase(String query);

    List<FoodNutrition>
    findTop10ByFoodNameContainingIgnoreCase(String query);

    @Query(value = """
SELECT *
FROM food_nutrition f
WHERE LOWER(f.food_name) = LOWER(:query)
   OR LOWER(f.food_name) LIKE LOWER(CONCAT(:query,' %'))
   OR LOWER(f.food_name) LIKE LOWER(CONCAT('% ',:query,' %'))
   OR LOWER(f.food_name) LIKE LOWER(CONCAT('% ',:query))
""", nativeQuery = true)
    List<FoodNutrition> searchByWord(String query);
}