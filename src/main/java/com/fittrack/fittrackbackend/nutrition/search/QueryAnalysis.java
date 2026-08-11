package com.fittrack.fittrackbackend.nutrition.search;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

public record QueryAnalysis(
    @JsonIgnore
    QueryIntent intent,
    boolean isBroadGeneric,
    Set<String> foodIdentityTerms,
    Set<String> modifierTerms,
    Set<String> preparationTerms,
    Set<String> compoundTerms,
    Set<String> brandTerms
) {}
