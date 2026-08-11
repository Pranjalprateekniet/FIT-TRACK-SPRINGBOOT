package com.fittrack.fittrackbackend.nutrition.search;

import com.fittrack.fittrackbackend.nutrition.entity.FoodSearchMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class SemanticScorer {

    private final QueryAnalyzer queryAnalyzer;

    public SemanticScorer(QueryAnalyzer queryAnalyzer) {
        this.queryAnalyzer = queryAnalyzer;
    }

    public double score(
            QueryAnalysis qa,
            FoodSearchMetadata candidate,
            NormalizedQuery nq) {
        return explain(qa, candidate, nq).finalFeatureScore();
    }

    public RankingScoreBreakdown explain(
            QueryAnalysis qa,
            FoodSearchMetadata candidate,
            NormalizedQuery nq) {
        List<String> reasons = new ArrayList<>();
        double score = 0.0;

        final double W_BASE = 0.500;
        final double W_PREP = 0.200;
        final double W_RECIPE_PEN = 0.150;
        final double W_BRAND_PEN = 0.150;
        final double W_COMPOUND = 0.300;
        final double W_BRAND = 0.300;
        final double W_MODIFIER = 0.200;

        List<String> candidateTokens = tokens(candidate.getNormalizedTokens());

        double baseScore = 0.0;
        double prepScore = 0.0;
        double modifierScore = 0.0;
        double compoundScore = 0.0;
        double brandScore = 0.0;
        double recipePen = 0.0;
        double brandPen = 0.0;

        /*
         * 1. BASE FOOD / GENERICITY
         */
        if (qa.isBroadGeneric()) {
            double genericity = f_genericity(qa, candidate, reasons);
            baseScore = W_BASE * genericity;
            score += baseScore;
        } else {
            double baseMatch = f_base_match(qa, candidate, reasons);
            baseScore = W_BASE * baseMatch;
            score += baseScore;
        }

        /*
         * 2. MODIFIERS
         */
        if (!qa.modifierTerms().isEmpty()) {
            double modifierMatch = f_modifier_match(qa, candidateTokens, reasons);

            modifierScore = W_MODIFIER * modifierMatch;
            score += modifierScore;
        }

        /*
         * 3. PREPARATIONS
         */
        if (!qa.preparationTerms().isEmpty()) {
            double prepMatch = f_prep_match(qa, candidate, reasons);

            prepScore = W_PREP * prepMatch;
            score += prepScore;
        }

        /*
         * 4. COMPOUNDS / RECIPES
         */
        if (!qa.compoundTerms().isEmpty()) {
            double compoundMatch = f_compound_match(qa, candidateTokens, reasons);

            compoundScore = W_COMPOUND * compoundMatch;
            score += compoundScore;
        } else {
            double recipePenalty = f_recipe_pen(candidate, reasons);

            recipePen = W_RECIPE_PEN * recipePenalty;
            score -= recipePen;
        }

        /*
         * 5. BRANDS
         */
        if (!qa.brandTerms().isEmpty()) {
            double brandMatch = f_brand_match(qa, candidateTokens, reasons);

            brandScore = W_BRAND * brandMatch;
            score += brandScore;
        } else {
            double brandPenalty = f_brand_pen(candidate, reasons);

            brandPen = W_BRAND_PEN * brandPenalty;
            score -= brandPen;
        }

        /*
         * Keep score inside the range expected by tier math.
         */
        double totalScore = Math.max(-0.500, Math.min(1.200, score));

        return new RankingScoreBreakdown(
                0.0,
                0.0,
                0.0,
                baseScore
                        + modifierScore
                        + prepScore
                        + compoundScore
                        + brandScore,
                0.0,
                -(recipePen + brandPen),
                totalScore,
                reasons);
    }

    /*
     * ============================================================
     * BASE FOOD MATCH
     * ============================================================
     */
    private double f_base_match(
            QueryAnalysis qa,
            FoodSearchMetadata candidate,
            List<String> reasons) {
        String baseFood = candidate.getBaseFoodCandidate();
        String confidence = candidate.getBaseFoodConfidence();

        if (baseFood != null
                && qa.foodIdentityTerms().contains(baseFood)) {

            if ("HIGH".equals(confidence)) {
                reasons.add(
                        "f_base_match: Exact HIGH confidence (+1.0)");
                return 1.0;
            }

            if ("MEDIUM".equals(confidence)) {
                reasons.add(
                        "f_base_match: Exact MEDIUM confidence (+0.5)");
                return 0.5;
            }
        }

        return 0.0;
    }

    /*
     * ============================================================
     * GENERIC QUERY SCORING
     *
     * This is the important fix.
     *
     * For:
     *
     * chicken
     *
     * "Chicken, raw" should beat:
     *
     * Chicken, ground, raw
     * Chicken, breast, raw
     * Chicken, thigh, raw
     *
     * because those are variants of the requested food rather
     * than the canonical generic food.
     * ============================================================
     */
    private double f_genericity(
            QueryAnalysis qa,
            FoodSearchMetadata candidate,
            List<String> reasons) {
        String baseFood = candidate.getBaseFoodCandidate();

        double baseIdentityScore = 0.0;

        String confidence = candidate.getBaseFoodConfidence();

        if (baseFood != null
                && qa.foodIdentityTerms().contains(baseFood)) {

            if ("HIGH".equals(confidence)) {
                baseIdentityScore = 0.70;
                reasons.add(
                        "Genericity: BaseIdentity HIGH (+0.70)");
            } else if ("MEDIUM".equals(confidence)) {
                baseIdentityScore = 0.40;
                reasons.add(
                        "Genericity: BaseIdentity MEDIUM (+0.40)");
            } else {
                baseIdentityScore = 0.20;
                reasons.add(
                        "Genericity: BaseIdentity LOW (+0.20)");
            }
        }

        /*
         * --------------------------------------------------------
         * 1. PENALIZE UNREQUESTED FOOD IDENTITIES
         * --------------------------------------------------------
         */
        double complexityPenalty = 0.0;

        QueryAnalysis candidateAnalysis = queryAnalyzer.analyze(
                tokens(candidate.getNormalizedTokens()));

        for (String candidateIdentity : candidateAnalysis.foodIdentityTerms()) {

            if (!qa.foodIdentityTerms().contains(candidateIdentity)) {

                complexityPenalty += 0.20;

                reasons.add(
                        "Genericity: Unrequested Base Food Identity "
                                + "Penalty ("
                                + candidateIdentity
                                + ") (-0.20)");
            }
        }

        /*
         * --------------------------------------------------------
         * 2. PENALIZE UNREQUESTED VARIANT/MODIFIER TERMS
         *
         * This is what fixes:
         *
         * chicken
         * ↓
         * Chicken, ground, raw
         *
         * "ground" was not requested, so the candidate should
         * lose ranking against a canonical chicken entry.
         * --------------------------------------------------------
         */
        double variantPenalty = 0.0;

        for (String candidateModifier : candidateAnalysis.modifierTerms()) {

            if (!qa.modifierTerms().contains(candidateModifier)) {

                variantPenalty += 0.20;

                reasons.add(
                        "Genericity: Unrequested Variant Modifier "
                                + "Penalty ("
                                + candidateModifier
                                + ") (-0.20)");
            }
        }

        /*
         * Do not let a long candidate accumulate an absurdly large
         * penalty from every descriptive token.
         */
        variantPenalty = Math.min(0.40, variantPenalty);

        if (variantPenalty > 0) {
            reasons.add(
                    String.format(
                            Locale.US,
                            "Genericity: Total Variant Penalty (-%.2f)",
                            variantPenalty));
        }

        /*
         * --------------------------------------------------------
         * 3. RECIPE PENALTY
         * --------------------------------------------------------
         */
        double recipePenalty = 0.0;

        if (Boolean.TRUE.equals(
                candidate.getIsRecipeIndicator())) {

            recipePenalty = 0.15;

            reasons.add(
                    "Genericity: Recipe Penalty (-0.15)");
        }

        /*
         * --------------------------------------------------------
         * 4. BRAND PENALTY
         * --------------------------------------------------------
         */
        double brandPenalty = 0.0;

        if (Boolean.TRUE.equals(
                candidate.getIsBrandedHeuristic())) {

            brandPenalty = 0.15;

            reasons.add(
                    "Genericity: Brand Penalty (-0.15)");
        }

        /*
         * --------------------------------------------------------
         * 5. RAW PREFERENCE
         *
         * Generic food searches prefer raw canonical entries when
         * the user did not specify a preparation.
         * --------------------------------------------------------
         */
        double rawBonus = 0.0;

        if ("RAW".equals(candidate.getPreparationState())
                && baseIdentityScore > 0) {

            rawBonus = 0.10;

            reasons.add(
                    "Genericity: RawPreferenceBonus (+0.10)");
        }

        /*
         * --------------------------------------------------------
         * FINAL GENERICITY SCORE
         * --------------------------------------------------------
         */
        double result = baseIdentityScore
                - complexityPenalty
                - variantPenalty
                - recipePenalty
                - brandPenalty
                + rawBonus;

        return Math.max(
                0.0,
                Math.min(1.0, result));
    }

    /*
     * ============================================================
     * MODIFIER MATCH
     * ============================================================
     */
    private double f_modifier_match(
            QueryAnalysis qa,
            List<String> candidateTokens,
            List<String> reasons) {
        if (qa.modifierTerms().isEmpty()) {
            return 0.0;
        }

        long count = qa.modifierTerms()
                .stream()
                .filter(candidateTokens::contains)
                .count();

        double ratio = (double) count / qa.modifierTerms().size();

        if (ratio > 0) {
            reasons.add(
                    String.format(
                            Locale.US,
                            "f_modifier_match: %.0f%% matched",
                            ratio * 100));
        }

        return ratio;
    }

    /*
     * ============================================================
     * PREPARATION MATCH
     * ============================================================
     */
    private double f_prep_match(
            QueryAnalysis qa,
            FoodSearchMetadata candidate,
            List<String> reasons) {
        List<String> candidatePreparations = tokens(candidate.getPreparationTerms());

        for (String preparation : qa.preparationTerms()) {

            if (candidatePreparations.contains(preparation)) {

                reasons.add(
                        "f_prep_match: Matched preparation (+1.0)");

                return 1.0;
            }
        }

        return 0.0;
    }

    /*
     * ============================================================
     * COMPOUND MATCH
     * ============================================================
     */
    private double f_compound_match(
            QueryAnalysis qa,
            List<String> candidateTokens,
            List<String> reasons) {
        for (String compound : qa.compoundTerms()) {

            if (candidateTokens.contains(compound)) {

                reasons.add(
                        "f_compound_match: Matched compound (+1.0)");

                return 1.0;
            }
        }

        return 0.0;
    }

    /*
     * ============================================================
     * BRAND MATCH
     * ============================================================
     */
    private double f_brand_match(
            QueryAnalysis qa,
            List<String> candidateTokens,
            List<String> reasons) {
        for (String brand : qa.brandTerms()) {

            if (candidateTokens.contains(brand)) {

                reasons.add(
                        "f_brand_match: Matched brand (+1.0)");

                return 1.0;
            }
        }

        return 0.0;
    }

    /*
     * ============================================================
     * RECIPE PENALTY
     * ============================================================
     */
    private double f_recipe_pen(
            FoodSearchMetadata candidate,
            List<String> reasons) {
        if (Boolean.TRUE.equals(
                candidate.getIsRecipeIndicator())) {

            double confidence = candidate.getRecipeIndicatorConfidence() != null
                    ? candidate.getRecipeIndicatorConfidence()
                            .doubleValue()
                    : 1.0;

            reasons.add(
                    String.format(
                            Locale.US,
                            "f_recipe_pen: Confidence %.2f",
                            confidence));

            return confidence;
        }

        return 0.0;
    }

    /*
     * ============================================================
     * BRAND PENALTY
     * ============================================================
     */
    private double f_brand_pen(
            FoodSearchMetadata candidate,
            List<String> reasons) {
        if (Boolean.TRUE.equals(
                candidate.getIsBrandedHeuristic())) {

            double confidence = candidate.getBrandConfidence() != null
                    ? candidate.getBrandConfidence()
                            .doubleValue()
                    : 1.0;

            reasons.add(
                    String.format(
                            Locale.US,
                            "f_brand_pen: Confidence %.2f",
                            confidence));

            return confidence;
        }

        return 0.0;
    }

    /*
     * ============================================================
     * TOKENIZATION
     * ============================================================
     */
    private List<String> tokens(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(
                value.toLowerCase(Locale.ROOT)
                        .split("\\s+"))
                .filter(token -> !token.isBlank())
                .toList();
    }
}