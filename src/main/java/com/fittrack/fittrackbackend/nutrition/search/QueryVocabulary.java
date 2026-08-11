package com.fittrack.fittrackbackend.nutrition.search;

import com.fittrack.fittrackbackend.nutrition.repository.FoodSearchMetadataRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Getter
public class QueryVocabulary {

    private final FoodSearchMetadataRepository metadataRepository;

    private Set<String> baseFoodVocab = new HashSet<>();
    private Set<String> prepVocab = Set.of("raw", "uncooked", "boiled", "hard-boiled", "fried", "scrambled", "baked", "grilled", "roasted", "steamed", "frozen", "dried", "canned", "smoked", "cooked", "fresh", "ground", "puree", "paste", "pickled", "sweetened");
    private Set<String> recipeVocab = Set.of("bread", "cake", "pie", "sandwich", "curry", "soup", "salad", "stew", "sauce", "omelet", "chips", "fries", "roll", "wrap", "smoothie", "pudding", "juice", "nuggets", "cookie");
    private Set<String> brandVocab = Set.of("mcdonalds", "mcdonald", "kelloggs", "kellogs", "pillsbury", "quaker", "ocean spray", "campbell", "kraft", "nabisco", "stouffer", "stouffers", "pepperidge", "lipton");

    @Autowired
    public QueryVocabulary(FoodSearchMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    QueryVocabulary(Set<String> baseFoodVocab, Set<String> prepVocab, Set<String> recipeVocab, Set<String> brandVocab) {
        this.metadataRepository = null;
        this.baseFoodVocab = baseFoodVocab;
        this.prepVocab = prepVocab;
        this.recipeVocab = recipeVocab;
        this.brandVocab = brandVocab;
    }

    @PostConstruct
    public void init() {
        if (metadataRepository != null) {
            this.baseFoodVocab = metadataRepository.findUniqueBaseFoodCandidates();
        }
    }
}
