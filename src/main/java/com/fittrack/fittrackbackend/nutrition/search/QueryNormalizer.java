package com.fittrack.fittrackbackend.nutrition.search;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class QueryNormalizer {

    private final QueryVocabulary vocabulary;

    private static final Set<String> STOP_WORDS = Set.of("the", "a", "an", "with", "and", "or");
    private static final Set<String> PRESERVED_WORDS = Set.of("whole", "raw", "white", "low", "free", "dark", "skim");

    public QueryNormalizer(QueryVocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public NormalizedQuery normalize(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return new NormalizedQuery("", List.of(), 0);
        }

        // 1. Lowercase
        String qLower = rawQuery.toLowerCase(Locale.ENGLISH).trim();

        // 2. Convert any Unicode whitespace to standard space
        String qStandardSpaces = qLower.replaceAll("(?U)\\s", " ");

        // 3. Remove punctuation except hyphen and standard space
        String qNoPunct = qStandardSpaces.replaceAll("[^a-z0-9 \\-]", "");

        // 4. Collapse whitespace
        String qCollapsed = qNoPunct.replaceAll(" +", " ").trim();

        // 5. Tokenization (split by space and hyphen)
        String[] splitTokens = qCollapsed.split("[ \\-]+");
        List<String> rawTokens = new ArrayList<>();
        for (String t : splitTokens) {
            if (!t.isBlank()) {
                rawTokens.add(t);
            }
        }

        // 6. Singular/Plural Normalization
        List<String> normalizedTokens = new ArrayList<>();
        for (String t : rawTokens) {
            String singularized = t;
            if (t.endsWith("es")) {
                String sub = t.substring(0, t.length() - 2);
                if (vocabulary.getBaseFoodVocab().contains(sub)) {
                    singularized = sub;
                }
            } else if (t.endsWith("s")) {
                String sub = t.substring(0, t.length() - 1);
                if (vocabulary.getBaseFoodVocab().contains(sub)) {
                    singularized = sub;
                }
            }
            normalizedTokens.add(singularized);
        }

        // 7. Stop-word Removal (only if size >= 3)
        List<String> finalTokens = new ArrayList<>();
        if (normalizedTokens.size() >= 3) {
            for (String t : normalizedTokens) {
                if (PRESERVED_WORDS.contains(t) || t.matches(".*\\d+.*")) {
                    finalTokens.add(t);
                } else if (!STOP_WORDS.contains(t)) {
                    finalTokens.add(t);
                }
            }
        } else {
            finalTokens.addAll(normalizedTokens);
        }

        String normalizedText = String.join(" ", finalTokens);
        return new NormalizedQuery(normalizedText, finalTokens, finalTokens.size());
    }
}
