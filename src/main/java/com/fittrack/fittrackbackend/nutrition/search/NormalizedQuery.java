package com.fittrack.fittrackbackend.nutrition.search;

import java.util.List;

public record NormalizedQuery(
    String normalizedText,
    List<String> tokens,
    int tokenCount
) {}
