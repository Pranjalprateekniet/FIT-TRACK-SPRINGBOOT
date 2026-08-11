package com.fittrack.fittrackbackend.nutrition.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    private String source;

    private Long fdcId;

    /**
     * Returns {@code true} when this food originates from the local
     * FitTrack dataset.  Local foods store {@code NULL} in the source
     * column — they never carry an explicit source tag.
     */
    public boolean isLocalFood() {
        return source == null;
    }

    /**
     * Returns {@code true} when this food was fetched from the USDA
     * FoodData Central API.  USDA foods carry a source tag that begins
     * with {@code "USDA_"} (e.g. {@code "USDA_FOUNDATION"}).
     */
    public boolean isUsdaFood() {
        return source != null && source.startsWith("USDA_");
    }


    /**
     * Named serving-size options for this food (e.g. "1 Egg", "100 g").
     * Cascading ALL ensures servings are persisted/removed with their parent food.
     * orphanRemoval removes a FoodServing row when it is de-listed from this collection.
     * {@code @JsonManagedReference} works in tandem with {@code @JsonBackReference} on
     * {@link FoodServing#food} to prevent infinite JSON recursion.
     */
    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<FoodServing> servings = new ArrayList<>();
}
