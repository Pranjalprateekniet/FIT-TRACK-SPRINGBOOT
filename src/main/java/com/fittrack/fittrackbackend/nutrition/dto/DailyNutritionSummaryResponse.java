package com.fittrack.fittrackbackend.nutrition.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyNutritionSummaryResponse {
    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbs;
    private Double totalFat;
    private Double totalFiber;
    private Double totalFreeSugar;
    private Double totalCholesterol;
}
