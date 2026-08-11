package com.fittrack.fittrackbackend.nutrition.client;

import com.fittrack.fittrackbackend.nutrition.dto.FoodSearchResponse;

/**
 * Internal transport record used to pass the fdcId alongside the public DTO
 * from the USDAClient to the FoodNutritionService, ensuring fdcId remains
 * completely encapsulated within the backend persistence layer.
 */
public record UsdaFoodResult(FoodSearchResponse dto, Long fdcId) {
}
