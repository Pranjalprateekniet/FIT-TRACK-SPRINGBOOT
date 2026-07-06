package com.fittrack.fittrackbackend.dto;

import com.fittrack.fittrackbackend.enums.WorkoutCategory;
import com.fittrack.fittrackbackend.enums.WorkoutExercise;
import com.fittrack.fittrackbackend.enums.WorkoutIntensity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateWorkoutRequest {
    @NotNull
    private WorkoutCategory category;

    @NotNull
    private WorkoutExercise exercise;

    private Integer sets;
    
    private Integer reps;

    @NotNull
    private WorkoutIntensity intensity;

    @Positive
    private Integer durationMinutes;

    @NotNull
    private LocalDate workoutDate;
}
