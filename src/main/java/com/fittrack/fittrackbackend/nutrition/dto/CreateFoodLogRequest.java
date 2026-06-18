package com.fittrack.fittrackbackend.nutrition.dto;


import com.fittrack.fittrackbackend.nutrition.entity.MealType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateFoodLogRequest {

    @NotNull
    private Long foodId;

    @NotNull
    @Positive
    private Double gramsConsumed;

    @NotNull
    private MealType mealType;


    private LocalDate logDate;

}
