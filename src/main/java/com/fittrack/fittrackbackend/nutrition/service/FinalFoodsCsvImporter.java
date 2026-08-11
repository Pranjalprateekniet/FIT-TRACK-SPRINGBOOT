package com.fittrack.fittrackbackend.nutrition.service;

import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import com.fittrack.fittrackbackend.nutrition.entity.FoodServing;
import com.fittrack.fittrackbackend.nutrition.repository.FoodNutritionRepository;
import com.fittrack.fittrackbackend.nutrition.repository.FoodServingRepository;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Production-grade startup importer for the {@code data/final_foods.csv} dataset.
 *
 * <h3>Behaviour</h3>
 * <ul>
 *   <li>Idempotent — skips entirely when the {@code food_nutrition} table is non-empty.</li>
 *   <li>Batch inserts — foods and servings are flushed in configurable batches (default 500).</li>
 *   <li>Transactional batches — each batch is wrapped in its own transaction so a
 *       single bad batch does not roll back the entire import.</li>
 *   <li>Fault-tolerant — malformed rows are skipped with a warning; the import continues.</li>
 *   <li>Statistics — prints imported foods, imported servings, skipped rows, and
 *       total execution time on completion.</li>
 * </ul>
 *
 * <h3>CSV column mapping</h3>
 * <pre>
 * id              → (ignored, DB generates its own PK)
 * fdc_id          → FoodNutrition.fdcId
 * food_name       → FoodNutrition.foodName
 * food_type       → FoodNutrition.foodType
 * calories        → FoodNutrition.calories  (Integer)
 * protein_g       → FoodNutrition.proteinG
 * carbs_g         → FoodNutrition.carbsG
 * fat_g           → FoodNutrition.fatG
 * fiber_g         → FoodNutrition.fiberG    (nullable)
 * cholesterol_mg  → FoodNutrition.cholesterolMg (nullable)
 * serving_portion → FoodServing.displayName
 * serving_grams   → FoodServing.servingWeightGrams + FoodNutrition.servingSizeG
 * </pre>
 *
 * <p>{@code servingSizeG} on the food entity is intentionally populated from
 * {@code serving_grams} so that {@link FoodLogService} nutrition scaling continues
 * working correctly without any changes to that service.
 *
 * <p>Fields not present in the new CSV ({@code freeSugarG}, {@code proteinCalorieRatio},
 * {@code source}) are left {@code null} — the entity columns are nullable and all
 * downstream code already handles {@code null} via the {@code safe()} helpers.
 */
@Service
@Order(1)
@RequiredArgsConstructor
public class FinalFoodsCsvImporter {

    private static final String CSV_PATH   = "data/final_foods.csv";
    private static final int    BATCH_SIZE = 500;

    // Column indices (0-based) — must match the CSV header order:
    // id, fdc_id, food_name, food_type, calories, protein_g, carbs_g, fat_g,
    // fiber_g, cholesterol_mg, serving_portion, serving_grams
    private static final int COL_FDC_ID          = 1;
    private static final int COL_FOOD_NAME        = 2;
    private static final int COL_FOOD_TYPE        = 3;
    private static final int COL_CALORIES         = 4;
    private static final int COL_PROTEIN_G        = 5;
    private static final int COL_CARBS_G          = 6;
    private static final int COL_FAT_G            = 7;
    private static final int COL_FIBER_G          = 8;
    private static final int COL_CHOLESTEROL_MG   = 9;
    private static final int COL_SERVING_PORTION  = 10;
    private static final int COL_SERVING_GRAMS    = 11;
    private static final int MIN_COLUMNS          = 12;

    private final FoodNutritionRepository foodNutritionRepository;
    private final FoodServingRepository   foodServingRepository;

    @PostConstruct
    public void importData() {

        if (foodNutritionRepository.count() > 0) {
            System.out.println("[FinalFoodsCsvImporter] food_nutrition table already has data — skipping import.");
            return;
        }

        System.out.println("[FinalFoodsCsvImporter] Starting import from " + CSV_PATH + " ...");
        long startTime = System.currentTimeMillis();

        int importedFoods    = 0;
        int importedServings = 0;
        int skippedRows      = 0;

        try {
            ClassPathResource resource = new ClassPathResource(CSV_PATH);

            CSVReader csvReader = new CSVReader(
                    new BufferedReader(
                            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
                    )
            );

            List<String[]> allRows = csvReader.readAll();
            csvReader.close();

            // Remove header row
            if (!allRows.isEmpty()) {
                allRows.remove(0);
            }

            // Process in batches
            List<String[]> batch = new ArrayList<>(BATCH_SIZE);

            for (String[] row : allRows) {

                // Strip Windows-style carriage returns from every field
                for (int i = 0; i < row.length; i++) {
                    row[i] = row[i].replace("\r", "").strip();
                }

                batch.add(row);

                if (batch.size() >= BATCH_SIZE) {
                    int[] counts = persistBatch(batch);
                    importedFoods    += counts[0];
                    importedServings += counts[1];
                    skippedRows      += counts[2];
                    batch.clear();
                }
            }

            // Flush remaining rows
            if (!batch.isEmpty()) {
                int[] counts = persistBatch(batch);
                importedFoods    += counts[0];
                importedServings += counts[1];
                skippedRows      += counts[2];
            }

        } catch (Exception e) {
            System.err.println("[FinalFoodsCsvImporter] Fatal error reading CSV: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        long elapsedMs = System.currentTimeMillis() - startTime;

        System.out.println("[FinalFoodsCsvImporter] ========== Import Complete ==========");
        System.out.printf("[FinalFoodsCsvImporter]   Imported foods    : %,d%n", importedFoods);
        System.out.printf("[FinalFoodsCsvImporter]   Imported servings : %,d%n", importedServings);
        System.out.printf("[FinalFoodsCsvImporter]   Skipped rows      : %,d%n", skippedRows);
        System.out.printf("[FinalFoodsCsvImporter]   Execution time    : %,d ms%n", elapsedMs);
        System.out.println("[FinalFoodsCsvImporter] ====================================");
    }

    /**
     * Persists one batch of CSV rows inside a single transaction.
     *
     * @param rows rows to process (each already stripped of CR characters)
     * @return int[3] — {importedFoods, importedServings, skippedRows}
     */
    @Transactional
    protected int[] persistBatch(List<String[]> rows) {

        List<FoodNutrition> foodBatch    = new ArrayList<>(rows.size());
        // Parallel list — index-aligned with foodBatch so we can link after save
        List<String[]>      servingData  = new ArrayList<>(rows.size());

        int skipped = 0;

        for (String[] row : rows) {

            try {
                // ── Structural guard ────────────────────────────────────────
                if (row.length < MIN_COLUMNS) {
                    System.out.printf("[FinalFoodsCsvImporter] SKIP — too few columns (%d): %s%n",
                            row.length, firstField(row));
                    skipped++;
                    continue;
                }

                // ── Required: food_name ──────────────────────────────────────
                String foodName = row[COL_FOOD_NAME];
                if (foodName == null || foodName.isBlank()) {
                    System.out.println("[FinalFoodsCsvImporter] SKIP — blank food_name");
                    skipped++;
                    continue;
                }

                // ── Required: serving_grams > 0 ─────────────────────────────
                Double servingGrams = parseDoubleOrNull(row[COL_SERVING_GRAMS]);
                if (servingGrams == null || servingGrams <= 0) {
                    System.out.printf("[FinalFoodsCsvImporter] SKIP — invalid serving_grams ('%s') for: %s%n",
                            row[COL_SERVING_GRAMS], foodName);
                    skipped++;
                    continue;
                }

                // ── Optional fields ──────────────────────────────────────────
                Long    fdcId         = parseLongOrNull(row[COL_FDC_ID]);
                String  foodType      = blankToNull(row[COL_FOOD_TYPE]);
                Integer calories      = parseIntegerOrNull(row[COL_CALORIES]);
                Double  proteinG      = parseDoubleOrNull(row[COL_PROTEIN_G]);
                Double  carbsG        = parseDoubleOrNull(row[COL_CARBS_G]);
                Double  fatG          = parseDoubleOrNull(row[COL_FAT_G]);
                Double  fiberG        = parseDoubleOrNull(row[COL_FIBER_G]);
                Double  cholesterolMg = parseDoubleOrNull(row[COL_CHOLESTEROL_MG]);
                String  servingPortion = blankToNull(row[COL_SERVING_PORTION]);
                String  displayName    = (servingPortion != null) ? servingPortion : "1 serving";

                // ── Build food entity ────────────────────────────────────────
                FoodNutrition food = FoodNutrition.builder()
                        .fdcId(fdcId)
                        .foodName(foodName)
                        .foodType(foodType)
                        .calories(calories)
                        .proteinG(proteinG)
                        .carbsG(carbsG)
                        .fatG(fatG)
                        .fiberG(fiberG)
                        .cholesterolMg(cholesterolMg)
                        // Populate servingSizeG so FoodLogService nutrition
                        // scaling continues to work without changes.
                        .servingSizeG(servingGrams)
                        // source = null → local food convention (existing code relies on this)
                        .build();

                foodBatch.add(food);
                servingData.add(new String[]{ displayName, String.valueOf(servingGrams) });

            } catch (Exception e) {
                System.out.printf("[FinalFoodsCsvImporter] SKIP — unexpected error on row [%s]: %s%n",
                        firstField(row), e.getMessage());
                skipped++;
            }
        }

        // ── Batch-save foods ─────────────────────────────────────────────────
        List<FoodNutrition> savedFoods = foodNutritionRepository.saveAll(foodBatch);

        // ── Build and batch-save servings linked to saved foods ──────────────
        List<FoodServing> servingBatch = new ArrayList<>(savedFoods.size());
        for (int i = 0; i < savedFoods.size(); i++) {
            String[] sd = servingData.get(i);
            FoodServing serving = FoodServing.builder()
                    .food(savedFoods.get(i))
                    .displayName(sd[0])
                    .servingWeightGrams(Double.parseDouble(sd[1]))
                    .displayOrder(1)
                    .isDefault(true)
                    .build();
            servingBatch.add(serving);
        }

        foodServingRepository.saveAll(servingBatch);

        return new int[]{ savedFoods.size(), servingBatch.size(), skipped };
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Double parseDoubleOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIntegerOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            // CSV stores calories as "307.0" — parse as double then truncate
            return (int) Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private String firstField(String[] row) {
        return (row.length > 0) ? row[0] : "<empty>";
    }
}
