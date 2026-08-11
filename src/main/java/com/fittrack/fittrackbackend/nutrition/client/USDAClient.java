package com.fittrack.fittrackbackend.nutrition.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.fittrackbackend.nutrition.dto.FoodSearchResponse;
import com.fittrack.fittrackbackend.nutrition.dto.ServingOptionResponse;
import com.fittrack.fittrackbackend.nutrition.service.usda.ServingNormalizer;
import com.fittrack.fittrackbackend.nutrition.service.usda.SearchResultRanker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class USDAClient {
    private final WebClient.Builder webClientBuilder;
    private final ServingNormalizer servingNormalizer;
    private final SearchResultRanker searchResultRanker;

    @Value("${usda.api.key}")
    private String apiKey;

    public List<UsdaFoodResult> searchFood(String query) {

        List<UsdaFoodResult> foods = searchFoodFromUsda(query, true);

        if (foods.isEmpty()) {

            System.out.println(
                    "No raw foods found. Falling back to all USDA foods.");

            foods = searchFoodFromUsda(query, false);
        }

        List<UsdaFoodResult> ranked = searchResultRanker.rank(foods, query);
        if (ranked.size() > 10) {
            ranked = ranked.subList(0, 10);
        }

        return ranked;
    }

    private List<UsdaFoodResult> searchFoodFromUsda(String query, boolean rawOnly) {

        String response = webClientBuilder.build()

                .get()
                .uri(uriBuilder -> {

                    uriBuilder
                            .scheme("https")
                            .host("api.nal.usda.gov")
                            .path("/fdc/v1/foods/search")
                            .queryParam("query", query)
                            .queryParam("pageSize", 50);
                    uriBuilder.queryParam("api_key", apiKey);

                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .block();
        System.out.println("RAW USDA RESPONSE:");
        System.out.println(response);

        try {

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(response);

            JsonNode foods = root.get("foods");

            System.out.println("Foods count = " + foods.size());

            List<UsdaFoodResult> result = new ArrayList<>();

            for (JsonNode food : foods) {

                String foodName = food.get("description").asText();

                String dataType = food.path("dataType").asText();

                Double calories = 0.0;
                Double protein = 0.0;
                Double carbs = 0.0;
                Double fat = 0.0;
                Double fiber = 0.0;
                Double cholesterol = 0.0;
                Double freeSugar = 0.0;
                Double servingSize = 100.0;

                if (food.has("servingSize")) {

                    servingSize = food.path("servingSize").asDouble();

                    if (servingSize <= 0) {
                        servingSize = 100.0;
                    }
                }

                JsonNode nutrients = food.get("foodNutrients");

                if (nutrients != null) {

                    for (JsonNode nutrient : nutrients) {

                        String nutrientName = nutrient.path("nutrientName").asText();
                        System.out.println(nutrientName);
                        Double value = nutrient.path("value").asDouble();

                        if (nutrientName.equalsIgnoreCase("Energy")) {
                            calories = value;
                        }

                        if (nutrientName.equalsIgnoreCase("Protein")) {
                            protein = value;
                        }

                        if (nutrientName.equalsIgnoreCase(
                                "Carbohydrate, by difference")) {

                            carbs = value;
                        }

                        if (nutrientName.equalsIgnoreCase(
                                "Total lipid (fat)")) {

                            fat = value;
                        }

                        if (nutrientName.equalsIgnoreCase(
                                "Fiber, total dietary")) {

                            fiber = value;
                        }

                        if (nutrientName.equalsIgnoreCase(
                                "Cholesterol")) {

                            cholesterol = value;
                        }

                        if (nutrientName.equalsIgnoreCase(
                                "Sugars, added")) {

                            freeSugar = value;
                        }
                        if (nutrientName.equalsIgnoreCase(
                                "Total Sugars") && freeSugar == 0.0) {

                            freeSugar = value;
                        }
                    }
                }
                System.out.println(
                        foodName + " -> " + dataType);

                // -----------------------------------------------------------------
                // Phase 1: Fetch household serving information from USDA food
                // details endpoint using fdcId. This enriches USDA search results
                // with serving size presets (e.g. "1 Large Egg = 50g").
                // -----------------------------------------------------------------
                long fdcId = food.path("fdcId").asLong(0L);
                List<ServingOptionResponse> servings = fetchServingsForFdcId(fdcId, foodName);

                FoodSearchResponse dto = new FoodSearchResponse(foodName,
                        calories,
                        protein,
                        carbs,
                        fat,
                        fiber,
                        cholesterol,
                        freeSugar,
                        servingSize,
                        dataType);

                dto.setServings(servings);
                result.add(new UsdaFoodResult(dto, fdcId));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse USDA response", e);
        }
    }

    // -------------------------------------------------------------------------
    // USDA Food Details — fetches foodPortions for a given fdcId
    // -------------------------------------------------------------------------

    /**
     * Calls GET /fdc/v1/food/{fdcId} and extracts household serving options
     * from the {@code foodPortions} array.
     *
     * <p>If the endpoint is unreachable, returns a parse error, or returns no
     * useful portions, an empty list is returned so that food search continues
     * normally (per Phase 1 error-handling requirements).
     *
     * @param fdcId    the USDA FoodData Central identifier
     * @param foodName the food name used only for debug logging
     * @return non-null list of {@link ServingOptionResponse}; may be empty
     */
    public List<ServingOptionResponse> fetchServingsForFdcId(long fdcId, String foodName) {

        if (fdcId <= 0) {
            System.out.println("[USDA-Servings] Skipping " + foodName + " — no fdcId");
            return new ArrayList<>();
        }

        try {
            String detailResponse = webClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.nal.usda.gov")
                            .path("/fdc/v1/food/" + fdcId)
                            .queryParam("api_key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (detailResponse == null || detailResponse.isBlank()) {
                System.out.println("[USDA-Servings] Empty response for fdcId=" + fdcId
                        + " food=" + foodName);
                return new ArrayList<>();
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(detailResponse);

            List<ServingOptionResponse> servings = extractServings(root, foodName, fdcId);
            return servings;

        } catch (Exception e) {
            // Step 7: Do NOT fail food search on detail endpoint failure
            System.out.println("[USDA-Servings] ERROR fetching details for fdcId="
                    + fdcId + " food=" + foodName + " — " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Parses the {@code foodPortions} array from the USDA food detail response
     * and converts every valid entry (those with a positive gramWeight) into a
     * {@link ServingOptionResponse}.
     *
     * <p>The first valid portion is marked {@code isDefault = true}; all others
     * are {@code false}.
     *
     * <p>Portion display names are built as: {@code "{amount} {modifier}"},
     * e.g. {@code "1 large"}, {@code "1 cup (4.86 large eggs)"}.
     *
     * @param root     the parsed JSON root node of the USDA food detail response
     * @param foodName used for debug logging
     * @param fdcId    used for debug logging
     * @return non-null, potentially empty list of serving options
     */
    private List<ServingOptionResponse> extractServings(JsonNode root,
                                                        String foodName,
                                                        long fdcId) {
        List<ServingOptionResponse> servings = new ArrayList<>();

        JsonNode portions = root.path("foodPortions");

        // Step 6: Debug logging
        System.out.println("[USDA-Servings] Food: " + foodName
                + " | fdcId: " + fdcId
                + " | foodPortions count: " + (portions.isMissingNode() ? 0 : portions.size()));

        if (portions.isMissingNode() || !portions.isArray() || portions.size() == 0) {
            System.out.println("[USDA-Servings]   -> No foodPortions available");
            return servings;
        }

        boolean firstValid = true;

        for (JsonNode portion : portions) {

            double gramWeight = portion.path("gramWeight").asDouble(0.0);

            // Step 4: Ignore entries that have no valid gram weight
            if (gramWeight <= 0) {
                System.out.println("[USDA-Servings]   -> Skipping portion with no gramWeight: "
                        + portion.toString());
                continue;
            }

            double amount   = portion.path("amount").asDouble(1.0);
            String modifier = portion.path("modifier").asText("").trim();

            // portionDescription is present in Survey (FNDDS) foods and already
            // contains the full human-readable label including quantity,
            // e.g. "1 banana", "1 cup, mashed".
            String portionDescription = portion.path("portionDescription").asText("").trim();

            // Skip useless USDA catch-all entries
            if (portionDescription.equalsIgnoreCase("Quantity not specified")) {
                System.out.println("[USDA-Servings]   -> Skipping 'Quantity not specified' portion");
                continue;
            }

            String displayName;

            if (!portionDescription.isEmpty()) {
                // Survey (FNDDS) foods: portionDescription is the authoritative label
                // (e.g. "1 banana"). Use it directly — do NOT prepend amount again.
                displayName = portionDescription;

            } else {
                // SR Legacy / Foundation foods: modifier is human-readable
                // (e.g. "large", "cup, sliced") but may also be a numeric USDA
                // category code (e.g. "60343") for some food types.  Skip numeric-only
                // modifiers and fall back to the measureUnit name.
                boolean modifierIsNumeric = modifier.matches("\\d+");

                if (modifier.isEmpty() || modifierIsNumeric) {
                    // Try measureUnit.name as last resort
                    JsonNode measureUnit = portion.path("measureUnit");
                    String unitName = measureUnit.path("name").asText("").trim();
                    if (!unitName.isEmpty() && !unitName.equalsIgnoreCase("undetermined")) {
                        modifier = unitName;
                    } else {
                        modifier = "";
                    }
                }

                if (modifier.isEmpty()) {
                    displayName = formatAmount(amount) + " serving";
                } else {
                    displayName = formatAmount(amount) + " " + modifier;
                }
            }

            boolean isDefault = firstValid;
            firstValid = false;

            // Step 6: Log every portion
            System.out.println("[USDA-Servings]   portion: " + displayName
                    + " | " + gramWeight + "g | isDefault=" + isDefault);

            servings.add(new ServingOptionResponse(
                    null,           // id = null for USDA-sourced servings (no DB row)
                    displayName,
                    gramWeight,
                    isDefault
            ));
        }

        return servingNormalizer.normalize(servings, foodName);
    }

    /**
     * Formats an amount value for display: drops the decimal when it is a
     * whole number (e.g. {@code 1.0} → {@code "1"}, {@code 0.5} → {@code "0.5"}).
     */
    private String formatAmount(double amount) {
        if (amount == Math.floor(amount) && !Double.isInfinite(amount)) {
            return String.valueOf((int) amount);
        }
        return String.valueOf(amount);
    }
}
