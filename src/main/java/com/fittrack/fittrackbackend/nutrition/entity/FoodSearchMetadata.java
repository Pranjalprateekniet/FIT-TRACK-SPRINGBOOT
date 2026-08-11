package com.fittrack.fittrackbackend.nutrition.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "food_search_metadata")
@Getter
@Setter
@NoArgsConstructor
public class FoodSearchMetadata {

    @Id
    @Column(name = "food_id")
    private Long foodId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "food_id")
    private FoodNutrition foodNutrition;

    @Column(name = "fdc_id")
    private Long fdcId;

    @Column(name = "food_name")
    private String foodName;

    @Column(name = "food_type", length = 50)
    private String foodType;

    @Column(name = "normalized_name", columnDefinition = "TEXT", nullable = false)
    private String normalizedName;

    @Column(name = "normalized_tokens", columnDefinition = "TEXT", nullable = false)
    private String normalizedTokens;

    @Column(name = "name_token_count", nullable = false)
    private Integer nameTokenCount;

    @Column(name = "has_comma", nullable = false)
    private Boolean hasComma;

    @Column(name = "has_parentheses", nullable = false)
    private Boolean hasParentheses;

    @Column(name = "has_digits", nullable = false)
    private Boolean hasDigits;

    @Column(name = "name_length_chars", nullable = false)
    private Integer nameLengthChars;

    @Column(name = "preparation_terms", columnDefinition = "TEXT")
    private String preparationTerms;

    @Column(name = "preparation_term", columnDefinition = "TEXT")
    private String preparationTerm;

    @Column(name = "preparation_state", length = 20)
    private String preparationState;

    @Column(name = "preparation_state_ambiguous")
    private Boolean preparationStateAmbiguous;

    @Column(name = "base_food_candidate", columnDefinition = "TEXT")
    private String baseFoodCandidate;

    @Column(name = "base_food_extraction_method", length = 25)
    private String baseFoodExtractionMethod;

    @Column(name = "base_food_confidence", length = 10)
    private String baseFoodConfidence;

    @Column(name = "is_recipe_indicator")
    private Boolean isRecipeIndicator;

    @Column(name = "recipe_indicator_term", columnDefinition = "TEXT")
    private String recipeIndicatorTerm;

    @Column(name = "recipe_indicator_confidence")
    private BigDecimal recipeIndicatorConfidence;

    @Column(name = "is_branded_heuristic")
    private Boolean isBrandedHeuristic;

    @Column(name = "brand_confidence")
    private BigDecimal brandConfidence;

    @Column(name = "brand_signal_detail", columnDefinition = "TEXT")
    private String brandSignalDetail;

    @Column(name = "canonical_food_id")
    private Long canonicalFoodId;

    @Column(name = "canonical_confidence", length = 10)
    private String canonicalConfidence;

    @Column(name = "duplicate_flag", length = 50)
    private String duplicateFlag;

    @Column(name = "duplicate_group_id")
    private Integer duplicateGroupId;

    @Transient
    private Double finalScore;

    @Transient
    private Integer finalTier;

    public int getFoodTypePriority() {
        if (foodType == null) return 3;
        switch (foodType) {
            case "foundation_food": return 1;
            case "sr_legacy_food": return 2;
            case "survey_fndds_food": return 3;
            default: return 4;
        }
    }
}
