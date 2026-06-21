package com.fittrack.fittrackbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateWorkoutRequest {
    @NotBlank
    private String title;
    @NotNull
    @Positive
    private Integer durationMinutes;
    @PositiveOrZero
    private Double caloriesBurned;
    private LocalDate workoutDate;
}
