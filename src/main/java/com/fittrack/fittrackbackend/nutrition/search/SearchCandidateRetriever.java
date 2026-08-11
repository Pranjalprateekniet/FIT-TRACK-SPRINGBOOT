package com.fittrack.fittrackbackend.nutrition.search;

import com.fittrack.fittrackbackend.nutrition.entity.FoodSearchMetadata;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SearchCandidateRetriever {

    private final EntityManager entityManager;
    private final QueryVocabulary vocabulary;

    public SearchCandidateRetriever(EntityManager entityManager, QueryVocabulary vocabulary) {
        this.entityManager = entityManager;
        this.vocabulary = vocabulary;
    }

    @Transactional(readOnly = true)
    public List<FoodSearchMetadata> retrieve(NormalizedQuery nq) {
        Set<FoodSearchMetadata> candidates = new LinkedHashSet<>();

        String query = nq.normalizedText();
        if (query.isBlank()) {
            return List.of();
        }

        // Query 1: Exact/Prefix
        String q1 = """
            SELECT m FROM FoodSearchMetadata m
            WHERE m.normalizedTokens = :q
               OR m.normalizedTokens LIKE :qPrefix
            ORDER BY m.foodType ASC, m.id ASC
            """;
        List<FoodSearchMetadata> q1Results = entityManager.createQuery(q1, FoodSearchMetadata.class)
                .setParameter("q", query)
                .setParameter("qPrefix", query + " %")
                .setMaxResults(50)
                .getResultList();
        candidates.addAll(q1Results);

        // Query 2: Token Substring (Any Order AND)
        // If query has multiple tokens, we want candidates that contain all tokens
        List<String> tokens = nq.tokens();
        if (tokens.size() > 1 && tokens.size() <= 4) {
            StringBuilder q2b = new StringBuilder("SELECT m FROM FoodSearchMetadata m WHERE ");
            for (int i = 0; i < tokens.size(); i++) {
                if (i > 0) q2b.append(" AND ");
                q2b.append("(m.normalizedTokens LIKE :t").append(i).append("Sub1")
                   .append(" OR m.normalizedTokens LIKE :t").append(i).append("Sub2")
                   .append(" OR m.normalizedTokens LIKE :t").append(i).append("Sub3")
                   .append(" OR m.normalizedTokens = :t").append(i).append(")");
            }
            q2b.append(" ORDER BY m.foodType ASC, m.id ASC");

            var query2 = entityManager.createQuery(q2b.toString(), FoodSearchMetadata.class);
            for (int i = 0; i < tokens.size(); i++) {
                String t = tokens.get(i);
                query2.setParameter("t" + i + "Sub1", "% " + t + " %");
                query2.setParameter("t" + i + "Sub2", t + " %");
                query2.setParameter("t" + i + "Sub3", "% " + t);
                query2.setParameter("t" + i, t);
            }

            List<FoodSearchMetadata> q2Results = query2.setMaxResults(100).getResultList();
            candidates.addAll(q2Results);
        } else {
            String q2 = """
                SELECT m FROM FoodSearchMetadata m
                WHERE m.normalizedTokens LIKE :qSub1
                   OR m.normalizedTokens LIKE :qSub2
                ORDER BY m.foodType ASC, m.id ASC
                """;
            List<FoodSearchMetadata> q2Results = entityManager.createQuery(q2, FoodSearchMetadata.class)
                    .setParameter("qSub1", "% " + query + " %")
                    .setParameter("qSub2", "% " + query)
                    .setMaxResults(100)
                    .getResultList();
            candidates.addAll(q2Results);
        }

        // Query 3: Per-Token Fallback (Only if candidates < 5)
        if (candidates.size() < 5) {
            for (String t : nq.tokens()) {
                String q3 = """
                    SELECT m FROM FoodSearchMetadata m
                    WHERE m.normalizedTokens LIKE :tSub1
                       OR m.normalizedTokens LIKE :tSub2
                       OR m.normalizedTokens LIKE :tSub3
                       OR m.normalizedTokens = :t
                    ORDER BY m.foodType ASC, m.id ASC
                    """;
                List<FoodSearchMetadata> q3Results = entityManager.createQuery(q3, FoodSearchMetadata.class)
                        .setParameter("tSub1", "% " + t + " %")
                        .setParameter("tSub2", t + " %")
                        .setParameter("tSub3", "% " + t)
                        .setParameter("t", t)
                        .setMaxResults(50)
                        .getResultList();
                candidates.addAll(q3Results);
                if (candidates.size() >= 150) break;
            }
        }

        // Typo Strategy (Fallback) if EXACTLY 0 results found
        if (candidates.isEmpty() && nq.tokens().size() <= 3) {
            boolean hasLongToken = nq.tokens().stream().anyMatch(t -> t.length() >= 4);
            if (hasLongToken) {
                for (String token : nq.tokens()) {
                    if (token.length() < 4) continue;
                    int maxDistance = token.length() > 5 ? 2 : 1;

                    String bestMatch = null;
                    int bestDist = maxDistance + 1;

                    for (String vocabWord : vocabulary.getBaseFoodVocab()) {
                        int dist = levenshteinDistance(token, vocabWord);
                        if (dist <= maxDistance && dist < bestDist) {
                            bestDist = dist;
                            bestMatch = vocabWord;
                        }
                    }

                    if (bestMatch != null) {
                        String qTypo = """
                            SELECT m FROM FoodSearchMetadata m
                            WHERE m.normalizedTokens = :q
                               OR m.normalizedTokens LIKE :qPrefix
                               OR m.normalizedTokens LIKE :qSub1
                               OR m.normalizedTokens LIKE :qSub2
                            ORDER BY m.foodType ASC, m.id ASC
                            """;
                        List<FoodSearchMetadata> typoResults = entityManager.createQuery(qTypo, FoodSearchMetadata.class)
                                .setParameter("q", bestMatch)
                                .setParameter("qPrefix", bestMatch + " %")
                                .setParameter("qSub1", "% " + bestMatch + " %")
                                .setParameter("qSub2", "% " + bestMatch)
                                .setMaxResults(50)
                                .getResultList();
                        candidates.addAll(typoResults);
                        break; // Stop after first typo correction helps
                    }
                }
            }
        }

        return candidates.stream().limit(150).collect(Collectors.toList());
    }

    private int levenshteinDistance(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
}
