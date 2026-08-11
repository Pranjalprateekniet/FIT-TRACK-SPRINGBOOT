package com.fittrack.fittrackbackend.nutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Lightweight projection of a {@link com.fittrack.fittrackbackend.nutrition.entity.FoodServing}
 * sent to the client alongside every food in a search result.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServingOptionResponse {

    /** Stable identifier — useful for future log-by-serving endpoints. */
    private UUID id;

    /** Human-readable chip label, e.g. "1 Egg", "Small Bowl". */
    private String displayName;

    /**
     * Gram weight represented by this serving.
     * The client passes this as {@code gramsConsumed} when the user picks a preset serving.
     */
    private Double servingWeightGrams;

    /** When {@code true} this serving should be pre-selected in the log modal. */
    private Boolean isDefault;
}
