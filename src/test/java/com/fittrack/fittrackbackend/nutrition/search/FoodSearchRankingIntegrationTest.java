package com.fittrack.fittrackbackend.nutrition.search;

import com.fittrack.fittrackbackend.nutrition.dto.FoodSearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FoodSearchRankingIntegrationTest {

    @Autowired
    private FoodSearchRankingService rankingService;

    @Test
    void broadGenericQueriesPrioritizeCanonicalBaseFoods() {
        assertFirst("apple", includes("apple", "raw"), excludes("dried", "baked", "juice", "pie"));
        assertFirst("banana", includes("banana", "raw"), excludes("baked", "split", "nectar", "chips"));
        assertFirst("egg", includes("egg", "whole", "raw"), excludes("substitute", "roll", "nog"));
        assertFirst("chicken", includes("chicken", "raw"), excludes("ground", "meatless", "feet", "nuggets"));
        assertFirst("milk", includes("milk"), excludes("sheep", "buttermilk", "imitation", "dried", "powder"));
        assertFirst("rice", includes("rice"), excludes("pudding", "cake", "cracker"));
        assertFirst("potato", includes("potato"), excludes("chips", "fries", "sweetened"));
        assertFirst("tomato", includes("tomato"), excludes("puree", "paste", "pickled", "sauce"));
    }

    @Test
    void requestedModifiersAreRewardedInsteadOfPenalized() {
        assertFirst("banana raw", includes("banana", "raw"), excludes("baked", "chips", "pudding"));
        assertFirst("banana chips", includes("banana", "chips"), excludes());
        assertFirst("banana pudding", includes("banana", "pudding"), excludes());
        assertFirst("egg raw", includes("egg", "raw"), excludes("roll", "substitute"));
        assertFirst("chicken breast", includes("chicken", "breast"), excludes("meatless", "feet"));
        assertFirst("chicken raw", includes("chicken", "raw"), excludes("nuggets", "sandwich"));
        assertFirst("boiled potato", includes("potato", "boiled"), excludes("chips", "fries"));
        assertFirst("tomato puree", includes("tomato", "puree"), excludes("pickled", "sauce"));
        assertFirst("rice cooked", includes("rice", "cooked"), excludes("pudding", "cake"));
    }

    private void assertFirst(String query, List<String> requiredTerms, List<String> forbiddenTerms) {
        List<FoodSearchResponse> results = rankingService.search(query);
        assertFalse(results.isEmpty(), () -> "Expected results for query: " + query);

        String firstName = results.get(0).getFoodName().toLowerCase(Locale.ENGLISH);
        for (String required : requiredTerms) {
            assertTrue(firstName.contains(required),
                    () -> query + " expected first result to contain '" + required + "' but was: " + results.get(0).getFoodName());
        }
        for (String forbidden : forbiddenTerms) {
            assertFalse(firstName.contains(forbidden),
                    () -> query + " expected first result not to contain '" + forbidden + "' but was: " + results.get(0).getFoodName());
        }
    }

    private List<String> includes(String... terms) {
        return List.of(terms);
    }

    private List<String> excludes(String... terms) {
        return List.of(terms);
    }
}
