package com.fittrack.fittrackbackend.nutrition.search;

import com.fittrack.fittrackbackend.nutrition.entity.FoodSearchMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class RetrievalTest {
    @Autowired
    private SearchCandidateRetriever retriever;
    @Autowired
    private QueryNormalizer normalizer;

    @Test
    void testOutput() {
        NormalizedQuery nq = normalizer.normalize("boiled potato");
        List<FoodSearchMetadata> results = retriever.retrieve(nq);
        System.out.println("=== RETRIEVED COUNT = " + results.size());
        boolean hasBoiledPotato = results.stream().anyMatch(r -> r.getFoodName().contains("Potato, boiled"));
        System.out.println("Has Potato, boiled? " + hasBoiledPotato);
        for(FoodSearchMetadata md : results) {
           if(md.getFoodName().contains("Potato, boiled")) {
               System.out.println("FOUND RETRIEVED: " + md.getFoodName());
           }
        }
    }
}
