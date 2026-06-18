package com.fittrack.fittrackbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateWorkoutRequest {

    @NotBlank
    private String title;
    @NotNull
    @PositiveOrZero
    private Integer durationMinutes;
    @NotNull
    @PositiveOrZero
    private Double caloriesBurned;
    @NotNull
    private LocalDate workoutDate;
}
