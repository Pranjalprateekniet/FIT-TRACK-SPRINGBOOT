package com.fittrack.fittrackbackend.nutrition.dto;

import com.fittrack.fittrackbackend.nutrition.entity.MealType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class FoodLogResponse {
    private String foodName;
    private Double gramsConsumed;
    private MealType mealType;
    private LocalDate logDate;
    private Long logId;
}
