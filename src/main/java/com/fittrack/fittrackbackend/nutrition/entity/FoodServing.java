package com.fittrack.fittrackbackend.nutrition.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Represents a named serving-size option for a {@link FoodNutrition} entry.
 * <p>
 * Examples: "1 Egg" (50 g), "Small Bowl" (150 g), "100 g" (100 g).
 * Each food can have multiple serving options; exactly one should be marked
 * {@code isDefault = true} to be pre-selected in the UI.
 * Serving options are ordered in the UI by {@code displayOrder}, not by weight.
 */
@Entity
@Table(name = "food_serving")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodServing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The food this serving option belongs to.
     * {@code @JsonBackReference} prevents infinite recursion when
     * FoodNutrition is serialized with its servings list.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_nutrition_id", nullable = false)
    @JsonBackReference
    private FoodNutrition food;

    /**
     * Human-readable label shown in the UI, e.g. "1 Egg", "Small Bowl", "100 g".
     */
    @Column(nullable = false)
    private String displayName;

    /**
     * Actual weight in grams that this serving represents.
     * Nutrition values are scaled from the food's per-100 g baseline using this weight.
     */
    @Column(nullable = false)
    private Double servingWeightGrams;

    /**
     * Controls the position of this serving option in the UI chip row.
     * Lower values appear first. Not related to serving weight.
     */
    @Column(nullable = false)
    private Integer displayOrder;

    /**
     * When {@code true} this serving is pre-selected when the user opens the log modal.
     * Only one serving per food should have this flag set.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}
