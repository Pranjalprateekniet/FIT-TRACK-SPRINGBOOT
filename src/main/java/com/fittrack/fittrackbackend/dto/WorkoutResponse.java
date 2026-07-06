package com.fittrack.fittrackbackend.dto;

import com.fittrack.fittrackbackend.enums.WorkoutCategory;
import com.fittrack.fittrackbackend.enums.WorkoutExercise;
import com.fittrack.fittrackbackend.enums.WorkoutIntensity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class WorkoutResponse {
    private UUID id;
    private WorkoutCategory category;
    private WorkoutExercise exercise;
    private Integer sets;
    private Integer reps;
    private WorkoutIntensity intensity;
    private Integer durationMinutes;
    private Integer estimatedDuration;
    private Double caloriesBurned;
    private LocalDate workoutDate;
}
