package com.fittrack.fittrackbackend.nutrition.repository;

import com.fittrack.fittrackbackend.nutrition.entity.FoodSearchMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FoodSearchMetadataRepository extends JpaRepository<FoodSearchMetadata, Long> {

    @Query("SELECT DISTINCT m.baseFoodCandidate FROM FoodSearchMetadata m WHERE m.baseFoodCandidate IS NOT NULL AND (m.baseFoodConfidence = 'HIGH' OR m.baseFoodConfidence = 'MEDIUM')")
    Set<String> findUniqueBaseFoodCandidates();
}
