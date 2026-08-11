package com.fittrack.fittrackbackend.nutrition.search;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

@SpringBootTest
class BenchmarkTest {
    @Autowired
    private RankingDebugger debugger;

    @Test
    void runBenchmark() {
        String[] queries = {
            "banana", "apple", "egg", "chicken", "milk", "rice", "potato", "tomato",
            "banana raw", "banana chips", "banana pudding", "egg raw", "chicken breast", "chicken raw", "boiled potato", "tomato puree", "rice cooked"
        };

        System.out.println("\n================= BENCHMARK RESULTS =================");
        for (String q : queries) {
            RankingDebugger.DebugResponse res = debugger.debug(q);
            String top = res.getResults().isEmpty() ? "NO RESULTS" : res.getResults().get(0).getFoodName();
            System.out.println("Query: '" + q + "' -> Top Result: " + top);
        }
        System.out.println("=====================================================\n");
    }
}
