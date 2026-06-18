package com.fittrack.fittrackbackend.nutrition.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="food_nutrition")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FoodNutrition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    private String foodName;

    private String foodType;

    private Integer calories;

    private Double carbsG;

    private Double proteinG;

    private Double fatG;

    private Double freeSugarG;

    private Double fiberG;

    private Double cholesterolMg;

    private Double servingSizeG;

    private Double proteinCalorieRatio;


}
