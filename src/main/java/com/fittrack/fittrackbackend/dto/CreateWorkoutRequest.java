package com.fittrack.fittrackbackend.dto;

import com.fittrack.fittrackbackend.enums.WorkoutCategory;
import com.fittrack.fittrackbackend.enums.WorkoutExercise;
import com.fittrack.fittrackbackend.enums.WorkoutIntensity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateWorkoutRequest {
    @NotNull
    private WorkoutCategory category;

    @NotNull
    private WorkoutExercise exercise;

    private Integer sets;
    
    private Integer reps;

    @NotNull
    private WorkoutIntensity intensity;

    // Optional because Strength training does not require it
    @Positive
    private Integer durationMinutes;

    private LocalDate workoutDate;
}
