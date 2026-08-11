package com.fittrack.fittrackbackend.nutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified search result DTO returned by {@code GET /api/nutrition/search}.
 * <p>
 * The {@code servings} list is populated for local-DB foods that have
 * pre-configured serving options; it is always non-null (empty list for USDA
 * results or foods with no serving data).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodSearchResponse {

    /** Database primary key — null only for raw USDA results not yet persisted. */
    private Long id;
    private String foodName;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double fiber;
    private Double cholesterol;
    private Double freeSugar;
    private Double servingSizeG;
    private String dataType;

    /**
     * Serving-size presets for this food.
     * Never {@code null} — defaults to an empty list when no presets exist.
     */
    private List<ServingOptionResponse> servings = new ArrayList<>();

    /**
     * Convenience constructor used by {@link com.fittrack.fittrackbackend.nutrition.client.USDAClient}
     * (10 positional args, no servings — keeps the USDA path unchanged).
     */
    public FoodSearchResponse(String foodName, Double calories, Double protein,
                              Double carbs, Double fat, Double fiber,
                              Double cholesterol, Double freeSugar,
                              Double servingSizeG, String dataType) {
        this.id           = null;  // USDA foods get an id after DB save
        this.foodName     = foodName;
        this.calories     = calories;
        this.protein      = protein;
        this.carbs        = carbs;
        this.fat          = fat;
        this.fiber        = fiber;
        this.cholesterol  = cholesterol;
        this.freeSugar    = freeSugar;
        this.servingSizeG = servingSizeG;
        this.dataType     = dataType;
        this.servings     = new ArrayList<>();
    }
}
