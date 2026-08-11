package com.fittrack.fittrackbackend.nutrition.service;

import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import com.fittrack.fittrackbackend.nutrition.entity.FoodServing;
import com.fittrack.fittrackbackend.nutrition.repository.FoodNutritionRepository;
import com.fittrack.fittrackbackend.nutrition.repository.FoodServingRepository;
import com.fittrack.fittrackbackend.nutrition.util.FoodNameNormalizer;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DISABLED — superseded by {@link FinalFoodsCsvImporter}.
 *
 * <p>The original implementation read {@code data/food_servings.csv} and matched
 * serving rows to local {@link FoodNutrition} entries by normalised food name.
 * That approach is replaced by the new single-file import pipeline which reads
 * {@code data/final_foods.csv} and persists both foods and their servings in one
 * transactional batch pass.
 *
 * <p>This class is kept for reference. The {@link #loadServings()} method now
 * returns immediately without loading anything.
 */
@Service
@RequiredArgsConstructor
@Order(2)
public class FoodServingDataLoader {

    private final FoodNutritionRepository foodNutritionRepository;
    private final FoodServingRepository   foodServingRepository;

    /**
     * DISABLED — {@link FinalFoodsCsvImporter} now owns serving import.
     */
    @PostConstruct
    public void loadServings() {
        // DISABLED — superseded by FinalFoodsCsvImporter which imports both
        // foods and servings from final_foods.csv. This loader targeted the old
        // food_servings.csv format and is kept for reference only.
    }
}
