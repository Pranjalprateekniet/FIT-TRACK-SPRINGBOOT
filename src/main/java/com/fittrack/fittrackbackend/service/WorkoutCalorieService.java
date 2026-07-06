package com.fittrack.fittrackbackend.service;

import com.fittrack.fittrackbackend.enums.WorkoutCategory;
import com.fittrack.fittrackbackend.enums.WorkoutExercise;
import com.fittrack.fittrackbackend.enums.WorkoutIntensity;
import org.springframework.stereotype.Service;

@Service
public class WorkoutCalorieService {

    public double calculateCaloriesBurned(WorkoutCategory category, WorkoutExercise exercise, WorkoutIntensity intensity, Integer estimatedDurationMinutes, Double weightKg) {
        if (estimatedDurationMinutes == null || weightKg == null || weightKg <= 0) {
            return 0.0;
        }

        double met = getMetValue(category, exercise, intensity);
        double durationHours = estimatedDurationMinutes / 60.0;

        return met * weightKg * durationHours;
    }

    public int estimateStrengthDuration(Integer sets, Integer reps, WorkoutIntensity intensity) {
        if (sets == null || reps == null) {
            return 0;
        }

        int activeTimeSeconds = sets * reps * 4;
        int restTimeSecondsPerSet = switch (intensity) {
            case LIGHT -> 45;
            case MODERATE -> 60;
            case HEAVY -> 90;
            case VERY_HEAVY -> 120;
            default -> 60;
        };

        // Rest is taken between sets, so (sets - 1) rest periods. Or we can just multiply by sets.
        // Based on prompt: Time = (sets x reps x 4s) + (rest time between sets)
        // I will assume (sets - 1) * restTimeSecondsPerSet, or just sets * restTimeSecondsPerSet for simplicity if sets=1.
        int restTimeSeconds = Math.max(0, (sets - 1)) * restTimeSecondsPerSet;
        
        int totalSeconds = activeTimeSeconds + restTimeSeconds;
        return (int) Math.ceil(totalSeconds / 60.0);
    }

    private double getMetValue(WorkoutCategory category, WorkoutExercise exercise, WorkoutIntensity intensity) {
        return switch (category) {
            case STRENGTH -> getStrengthMet(intensity);
            case YOGA -> getYogaMet(intensity, exercise);
            case CARDIO -> getCardioMet(exercise, intensity);
        };
    }

    private double getStrengthMet(WorkoutIntensity intensity) {
        return switch (intensity) {
            case LIGHT -> 3.5;
            case MODERATE -> 5.0;
            case HEAVY -> 6.0;
            case VERY_HEAVY -> 8.0;
            default -> 5.0;
        };
    }

    private double getYogaMet(WorkoutIntensity intensity, WorkoutExercise exercise) {
        if (exercise == WorkoutExercise.POWER_YOGA) return 5.5;
        if (exercise == WorkoutExercise.SURYA_NAMASKAR) return 6.5;
        
        return switch (intensity) {
            case LIGHT -> 2.5;
            case MODERATE -> 3.5;
            case ADVANCED -> 5.5;
            default -> 3.5;
        };
    }

    private double getCardioMet(WorkoutExercise exercise, WorkoutIntensity intensity) {
        // Base METs based on the prompt's examples
        double baseMet = switch (exercise) {
            case WALKING -> 3.5;
            case JOGGING -> 7.0;
            case RUNNING -> 10.0;
            case CYCLING -> 8.0;
            case SKIPPING_ROPE -> 12.0;
            case SWIMMING -> 8.0;
            case STAIR_CLIMBER -> 9.0;
            case ROWING -> 7.0;
            case TREADMILL -> 9.0;
            case ELLIPTICAL -> 5.0;
            case STATIONARY_BIKE -> 7.0;
            default -> 7.0;
        };

        // Adjust slightly based on intensity for cardio if desired, or just return baseMet.
        // Prompt says "Use MET values according to activity", I will apply a small multiplier if intensity is specified.
        return switch (intensity) {
            case LIGHT -> baseMet * 0.8;
            case MODERATE -> baseMet * 1.0;
            case VIGOROUS -> baseMet * 1.2;
            case VERY_VIGOROUS -> baseMet * 1.4;
            default -> baseMet;
        };
    }
}
