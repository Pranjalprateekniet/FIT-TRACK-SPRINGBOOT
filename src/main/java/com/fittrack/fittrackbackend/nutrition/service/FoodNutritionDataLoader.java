package com.fittrack.fittrackbackend.nutrition.service;

import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import com.fittrack.fittrackbackend.nutrition.repository.FoodNutritionRepository;
import com.fittrack.fittrackbackend.nutrition.util.FoodNameNormalizer;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * DISABLED — superseded by {@link FinalFoodsCsvImporter}.
 *
 * <p>The original implementation read {@code data/food_nutrition_cleaned.csv} (11 columns)
 * and bulk-inserted {@link FoodNutrition} records on startup.  That dataset and loading
 * strategy are replaced by the new single-file import pipeline which reads
 * {@code data/final_foods.csv} (12 columns, including serving data) via
 * {@link FinalFoodsCsvImporter}.
 *
 * <p>This class is kept for reference. The {@link #loadData()} method now
 * returns immediately without loading anything.
 */
@RequiredArgsConstructor
@Service
@Order(1)
public class FoodNutritionDataLoader {

    private final FoodNutritionRepository foodNutritionRepository;

    /**
     * DISABLED — {@link FinalFoodsCsvImporter} now owns food import.
     */
    @PostConstruct
    public void loadData() {
        // DISABLED — superseded by FinalFoodsCsvImporter which imports the
        // production dataset (final_foods.csv). This loader targeted the old
        // food_nutrition_cleaned.csv format (11 columns) and is kept for reference.
    }
}
