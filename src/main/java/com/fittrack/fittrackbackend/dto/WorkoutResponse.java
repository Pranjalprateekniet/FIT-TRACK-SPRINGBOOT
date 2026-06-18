package com.fittrack.fittrackbackend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class WorkoutResponse {
    private UUID id;
    private String title;
    private Integer durationMinutes;
    private Double caloriesBurned;
    private LocalDate workoutDate;

}
