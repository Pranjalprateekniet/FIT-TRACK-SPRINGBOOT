package com.fittrack.fittrackbackend.nutrition.search;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class QueryAnalyzer {

    private final QueryVocabulary vocabulary;

    public QueryAnalyzer(QueryVocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public QueryAnalysis analyze(NormalizedQuery nq) {
        return analyze(nq.tokens());
    }

    public QueryAnalysis analyze(List<String> tokens) {
        Set<String> foodIdentityTerms = new HashSet<>();
        Set<String> modifierTerms = new HashSet<>();
        Set<String> preparationTerms = new HashSet<>();
        Set<String> compoundTerms = new HashSet<>();
        Set<String> brandTerms = new HashSet<>();

        int n = tokens.size();
        boolean[] consumed = new boolean[n];

        // Generate N-grams from longest to shortest
        for (int length = n; length > 0; length--) {
            for (int i = 0; i <= n - length; i++) {
                // Check if any token in this range is already consumed
                boolean overlap = false;
                for (int j = i; j < i + length; j++) {
                    if (consumed[j]) {
                        overlap = true;
                        break;
                    }
                }
                if (overlap) continue;

                // Build N-gram
                StringBuilder sb = new StringBuilder();
                for (int j = i; j < i + length; j++) {
                    sb.append(tokens.get(j));
                    if (j < i + length - 1) sb.append(" ");
                }
                String ngram = sb.toString();

                boolean matched = false;
                if (vocabulary.getBaseFoodVocab().contains(ngram)) {
                    foodIdentityTerms.add(ngram);
                    matched = true;
                } else if (vocabulary.getPrepVocab().contains(ngram)) {
                    preparationTerms.add(ngram);
                    matched = true;
                } else if (vocabulary.getRecipeVocab().contains(ngram)) {
                    compoundTerms.add(ngram);
                    matched = true;
                } else if (vocabulary.getBrandVocab().contains(ngram)) {
                    brandTerms.add(ngram);
                    matched = true;
                }

                if (matched) {
                    for (int j = i; j < i + length; j++) {
                        consumed[j] = true;
                    }
                }
            }
        }

        // Remaining unassigned tokens are modifiers
        for (int i = 0; i < n; i++) {
            if (!consumed[i]) {
                modifierTerms.add(tokens.get(i));
            }
        }

        if (foodIdentityTerms.isEmpty() && preparationTerms.isEmpty() && compoundTerms.isEmpty()
                && brandTerms.isEmpty() && n <= 3) {
            recoverTypoFoodIdentity(tokens, foodIdentityTerms, modifierTerms);
        }

        QueryIntent intent;
        if (!brandTerms.isEmpty()) {
            intent = QueryIntent.BRAND;
        } else if (!compoundTerms.isEmpty()) {
            intent = QueryIntent.COMPOUND_RECIPE;
        } else if (!preparationTerms.isEmpty()) {
            intent = QueryIntent.PREPARATION;
        } else if (hasSubtypeIntent(foodIdentityTerms, modifierTerms, n)) {
            intent = QueryIntent.SUBTYPE;
        } else {
            intent = QueryIntent.BROAD_BASE_FOOD;
        }

        boolean isBroadGeneric = compoundTerms.isEmpty()
                && brandTerms.isEmpty()
                && preparationTerms.isEmpty()
                && modifierTerms.isEmpty()
                && !foodIdentityTerms.isEmpty();

        return new QueryAnalysis(intent, isBroadGeneric, foodIdentityTerms, modifierTerms, preparationTerms, compoundTerms, brandTerms);
    }

    private void recoverTypoFoodIdentity(List<String> tokens, Set<String> foodIdentityTerms, Set<String> modifierTerms) {
        for (String token : tokens) {
            if (token.length() < 4) {
                continue;
            }

            int maxDistance = token.length() > 5 ? 2 : 1;
            String bestMatch = null;
            int bestDistance = maxDistance + 1;

            for (String vocabWord : vocabulary.getBaseFoodVocab()) {
                int distance = levenshteinDistance(token, vocabWord);
                if (distance <= maxDistance && distance < bestDistance) {
                    bestDistance = distance;
                    bestMatch = vocabWord;
                }
            }

            if (bestMatch != null) {
                foodIdentityTerms.add(bestMatch);
                modifierTerms.remove(token);
                return;
            }
        }
    }

    private boolean hasSubtypeIntent(Set<String> foodIdentityTerms, Set<String> modifierTerms, int tokenCount) {
        return !modifierTerms.isEmpty();
    }

    private int levenshteinDistance(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
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
