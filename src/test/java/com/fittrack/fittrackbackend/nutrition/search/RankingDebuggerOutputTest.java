package com.fittrack.fittrackbackend.nutrition.search;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fittrack.fittrackbackend.nutrition.entity.FoodSearchMetadata;
import com.fittrack.fittrackbackend.nutrition.repository.FoodSearchMetadataRepository;
import java.util.List;

@SpringBootTest
class RankingDebuggerOutputTest {
    @Autowired
    private RankingDebugger rankingDebugger;
    @Test
    void testOutput() {
        System.out.println("\nQUERY: chicken");
        RankingDebugger.DebugResponse resp = rankingDebugger.debug("chicken");
        for (int i=0; i<Math.min(20, resp.getResults().size()); i++) {
            System.out.println(resp.getResults().get(i).getFoodName() + " | tokens=" + resp.getResults().get(i).getNormalizedTokens());
        }
    }
}
