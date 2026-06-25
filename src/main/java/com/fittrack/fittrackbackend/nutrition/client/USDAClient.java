package com.fittrack.fittrackbackend.nutrition.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.fittrackbackend.nutrition.dto.FoodSearchResponse;
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

    @Value("${usda.api.key}")
    private String apiKey;

    public List<FoodSearchResponse> searchFood(String query) {

        String response= webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.nal.usda.gov")
                        .path("/fdc/v1/foods/search")
                        .queryParam("query", query)
                        .queryParam("pageSize", 10) // IMPORTANT
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(response);

            JsonNode foods = root.get("foods");

            List<FoodSearchResponse> result = new ArrayList<>();

            for (JsonNode food : foods) {

                String foodName = food.get("description").asText();

                Double calories = 0.0;
                Double protein = 0.0;
                Double carbs = 0.0;
                Double fat = 0.0;

                JsonNode nutrients = food.get("foodNutrients");

                if (nutrients != null) {

                    for (JsonNode nutrient : nutrients) {

                        String nutrientName =
                                nutrient.path("nutrientName").asText();

                        Double value =
                                nutrient.path("value").asDouble();

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
                    }
                }

                result.add(
                        new FoodSearchResponse(
                                foodName,
                                calories,
                                protein,
                                carbs,
                                fat
                        )
                );
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse USDA response", e);
        }
    }
}
