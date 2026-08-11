package com.fittrack.fittrackbackend.nutrition.service.usda;

import com.fittrack.fittrackbackend.nutrition.dto.ServingOptionResponse;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ServingDuplicateResolver {

    public List<ServingOptionResponse> resolve(List<ServingOptionResponse> servings) {
        List<ServingOptionResponse> resolved = new ArrayList<>();

        // Map from gramWeight to list of servings with that weight
        Map<Double, List<ServingOptionResponse>> weightMap = new LinkedHashMap<>();

        for (ServingOptionResponse serving : servings) {
            weightMap.computeIfAbsent(serving.getServingWeightGrams(), k -> new ArrayList<>()).add(serving);
        }

        for (List<ServingOptionResponse> weightGroup : weightMap.values()) {
            if (weightGroup.size() == 1) {
                resolved.add(weightGroup.get(0));
            } else {
                resolved.addAll(resolveConceptualDuplicates(weightGroup));
            }
        }

        return resolved;
    }

    private List<ServingOptionResponse> resolveConceptualDuplicates(List<ServingOptionResponse> group) {
        List<ServingOptionResponse> result = new ArrayList<>();
        Set<String> concepts = new HashSet<>();

        // Sort the group by "user-friendliness" before resolving.
        // We prefer shorter names, names without "Serving", etc.
        List<ServingOptionResponse> sortedGroup = new ArrayList<>(group);
        sortedGroup.sort(Comparator.comparingInt(this::getPenaltyScore));

        for (ServingOptionResponse serving : sortedGroup) {
            String concept = extractConcept(serving.getDisplayName());
            if (concepts.add(concept)) {
                result.add(serving);
            }
        }

        return result;
    }

    private String extractConcept(String label) {
        if (label == null) return "";
        String normalized = label.toLowerCase()
                .replaceAll("^\\d+(\\.\\d+)?\\s*", "") // remove leading number
                .trim();

        // Remove common sizing words to find the core concept.
        normalized = normalized.replaceAll("\\b(medium|large|small|serving)\\b", "").trim();
        return normalized;
    }

    private int getPenaltyScore(ServingOptionResponse serving) {
        String label = serving.getDisplayName().toLowerCase();
        int penalty = 0;
        if (label.contains("serving")) penalty += 50;
        if (label.contains("nlea")) penalty += 100;
        penalty += label.length(); // shorter is better
        return penalty;
    }
}
