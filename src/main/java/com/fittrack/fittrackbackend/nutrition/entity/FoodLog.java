package com.fittrack.fittrackbackend.nutrition.entity;

import com.fittrack.fittrackbackend.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="food_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class FoodLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "food_id")
    private FoodNutrition foodNutrition;

    @Column(nullable = false)
    private Double gramsConsumed;
    @Enumerated(EnumType.STRING)
    private MealType mealType;

    @Column(nullable = false)
    private LocalDate logDate;

    private LocalDateTime createdAt;

}
