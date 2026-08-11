package com.fittrack.fittrackbackend.nutrition.service.usda;

import org.springframework.stereotype.Component;

@Component
public class ServingLabelFormatter {

    public String extractBaseFoodName(String rawFoodName) {
        if (rawFoodName == null || rawFoodName.isEmpty()) return "";
        // Take everything before the first comma
        String baseName = rawFoodName.split(",")[0].trim().toLowerCase();

        // Basic singularization for common cases
        if (baseName.endsWith("s") && !baseName.endsWith("ss") && !baseName.endsWith("is") && !baseName.endsWith("us")) {
            if (baseName.endsWith("ies")) {
                baseName = baseName.substring(0, baseName.length() - 3) + "y";
            } else if (baseName.endsWith("es") && (baseName.endsWith("ches") || baseName.endsWith("shes") || baseName.endsWith("xes"))) {
                baseName = baseName.substring(0, baseName.length() - 2);
            } else if (baseName.endsWith("oes")) {
                baseName = baseName.substring(0, baseName.length() - 2);
            } else {
                baseName = baseName.substring(0, baseName.length() - 1);
            }
        }
        return baseName;
    }

    public String format(String label, String rawFoodName) {
        if (label == null) return "";
        String cleaned = label.trim();

        // Discard "Quantity not specified"
        if (cleaned.equalsIgnoreCase("Quantity not specified")) {
            return null;
        }

        // 1 RACC -> 1 Serving
        cleaned = cleaned.replaceAll("(?i)\\b1 RACC\\b", "1 Serving");

        // Remove (NFS) and other specific abbreviations
        cleaned = cleaned.replaceAll("(?i)\\s*\\(NFS\\)", "");
        cleaned = cleaned.replaceAll("(?i)\\bNFS\\b", "");

        // Handle "NLEA serving"
        if (cleaned.toLowerCase().contains("nlea serving") || cleaned.toLowerCase().contains("nlea")) {
            String baseFoodName = extractBaseFoodName(rawFoodName);
            if (!baseFoodName.isEmpty()) {
                cleaned = "1 Medium " + toTitleCase(baseFoodName);
            } else {
                cleaned = "1 Medium Serving";
            }
        }

        // Title Case and clean up punctuation
        cleaned = toTitleCase(cleaned);

        // Handle specific lowercase acronyms
        cleaned = cleaned.replace("Fl Oz", "fl oz");
        cleaned = cleaned.replace(" fl oz", " fl oz"); // Ensure it remains lowercase

        // "1 Cup, Mashed" -> "1 Cup Mashed"
        cleaned = cleaned.replace(", ", " ").replace(",", " ");

        return cleaned.trim();
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c) || c == '(' || c == ')' || c == '-' || c == '/') {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toTitleCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
