package com.fittrack.fittrackbackend.goals.entity;

import com.fittrack.fittrackbackend.entity.User;
import com.fittrack.fittrackbackend.goals.enums.ActivityLevel;
import com.fittrack.fittrackbackend.goals.enums.Gender;
import com.fittrack.fittrackbackend.goals.enums.GoalPace;
import com.fittrack.fittrackbackend.goals.enums.GoalType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GoalEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalType goalType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalPace goalPace;

    @Column(nullable = false)
    private Integer targetCalories;

    @Column(nullable = false)
    private Double targetProtein;

    @Column(nullable = false)
    private Double targetCarbohydrates;

    @Column(nullable = false)
    private Double targetFat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityLevel activityLevel;

    @Column(nullable = false)
    private Double weightKg;

    @Column(nullable = false)
    private Double heightCm;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Double bmi;

    @Column(nullable = false)
    private String bmiCategory;

    @Column(nullable = false)
    private Double targetWeightKg;

    @PrePersist
    private void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt=createdAt;
    }
    @PreUpdate
    private void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
