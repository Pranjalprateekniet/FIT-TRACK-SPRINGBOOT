package com.fittrack.fittrackbackend.nutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class FoodSearchResponse {
    private String foodName;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
}
