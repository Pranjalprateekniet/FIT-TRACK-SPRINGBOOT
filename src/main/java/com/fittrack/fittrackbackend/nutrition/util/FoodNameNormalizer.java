package com.fittrack.fittrackbackend.nutrition.util;

public final class FoodNameNormalizer {

    public static String normalize(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        return name.toLowerCase()
                   .trim()
                   .replaceAll("\\s+", " ") // collapse multiple spaces
                   .replaceAll("[^a-z0-9\\s]", ""); // remove punctuation
    }

    private FoodNameNormalizer() {
        // Prevent instantiation
    }
}
