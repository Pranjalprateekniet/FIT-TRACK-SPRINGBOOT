package com.fittrack.fittrackbackend.goals.repository;

import com.fittrack.fittrackbackend.entity.User;
import com.fittrack.fittrackbackend.goals.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {
    Optional<GoalEntity> findByUser(User user);
}
