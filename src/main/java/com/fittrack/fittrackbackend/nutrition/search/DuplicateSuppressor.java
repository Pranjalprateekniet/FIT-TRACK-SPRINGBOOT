package com.fittrack.fittrackbackend.nutrition.search;

import com.fittrack.fittrackbackend.nutrition.entity.FoodSearchMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DuplicateSuppressor {

    public List<FoodSearchMetadata> suppress(List<FoodSearchMetadata> sortedCandidates) {
        Set<Integer> seenGroups = new HashSet<>();
        List<FoodSearchMetadata> output = new ArrayList<>();

        for (FoodSearchMetadata candidate : sortedCandidates) {
            Integer groupId = candidate.getDuplicateGroupId();

            if (groupId == null) {
                output.add(candidate);
            } else if (!seenGroups.contains(groupId)) {
                output.add(candidate);
                seenGroups.add(groupId);
            }

            if (output.size() >= 20) {
                break;
            }
        }

        return output;
    }
}
