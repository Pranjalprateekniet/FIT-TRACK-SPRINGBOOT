package com.fittrack.fittrackbackend.repository;

import com.fittrack.fittrackbackend.entity.User;
import com.fittrack.fittrackbackend.entity.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutRepository extends JpaRepository<Workout, UUID> {
    List<Workout> findByUser(User user);
    Optional<Workout> findById(UUID id);


}

