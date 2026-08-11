package com.fittrack.fittrackbackend.nutrition.repository;

import com.fittrack.fittrackbackend.nutrition.entity.FoodServing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FoodServingRepository extends JpaRepository<FoodServing, UUID> {

    /** All serving options for a given food, ordered by display position. */
    List<FoodServing> findByFoodIdOrderByDisplayOrderAsc(Long foodId);

    /** The default serving option for a given food (should be exactly one). */
    Optional<FoodServing> findByFoodIdAndIsDefaultTrue(Long foodId);
}
