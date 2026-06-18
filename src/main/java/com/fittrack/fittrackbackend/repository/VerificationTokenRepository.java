package com.fittrack.fittrackbackend.repository;

import com.fittrack.fittrackbackend.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken>findByToken(String token);

}
