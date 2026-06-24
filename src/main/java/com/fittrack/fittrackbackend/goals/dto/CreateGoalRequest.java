package com.fittrack.fittrackbackend.goals.dto;

import com.fittrack.fittrackbackend.goals.enums.ActivityLevel;
import com.fittrack.fittrackbackend.goals.enums.Gender;
import com.fittrack.fittrackbackend.goals.enums.GoalPace;
import com.fittrack.fittrackbackend.goals.enums.GoalType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateGoalRequest {
    @NotNull
    private GoalType goalType;

    @NotNull
    private GoalPace goalPace;

    @NotNull
    private Gender gender;

    @NotNull
    private ActivityLevel activityLevel;

    @NotNull
    @DecimalMin("30.0")
    @DecimalMax("300.0")
    private Double weightKg;

    @NotNull
    @DecimalMin("100.0")
    @DecimalMax("250.0")
    private Double heightCm;

    @NotNull
    @DecimalMin("30.0")
    @DecimalMax("300.0")
    private Double targetWeightKg;

    @NotNull
    @Min(13)
    @Max(100)
    private Integer age;
}
