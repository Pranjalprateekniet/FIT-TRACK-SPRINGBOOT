package com.fittrack.fittrackbackend.nutrition.repository;

import com.fittrack.fittrackbackend.entity.User;
import com.fittrack.fittrackbackend.nutrition.entity.FoodLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FoodLogRepository extends JpaRepository<FoodLog,Long> {
    List<FoodLog>findByUserAndLogDate(User user, LocalDate localDate);
}
