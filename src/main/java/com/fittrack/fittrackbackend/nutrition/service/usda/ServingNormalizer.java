package com.fittrack.fittrackbackend.nutrition.service.usda;

import com.fittrack.fittrackbackend.nutrition.dto.ServingOptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ServingNormalizer {

    private final ServingLabelFormatter labelFormatter;
    private final ServingDuplicateResolver duplicateResolver;
    private final ServingDefaultSelector defaultSelector;

    public List<ServingOptionResponse> normalize(List<ServingOptionResponse> rawServings, String foodName) {
        if (rawServings == null || rawServings.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Format labels
        List<ServingOptionResponse> formatted = rawServings.stream()
                .map(s -> {
                    String newLabel = labelFormatter.format(s.getDisplayName(), foodName);
                    if (newLabel == null) return null; // discarded
                    return new ServingOptionResponse(s.getId(), newLabel, s.getServingWeightGrams(), s.getIsDefault());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 2. Resolve duplicates
        List<ServingOptionResponse> deduplicated = duplicateResolver.resolve(formatted);

        // 3. Select default
        String baseFoodName = labelFormatter.extractBaseFoodName(foodName);
        defaultSelector.selectDefault(deduplicated, baseFoodName);

        return deduplicated;
    }
}
