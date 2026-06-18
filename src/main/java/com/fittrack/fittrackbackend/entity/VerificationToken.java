package com.fittrack.fittrackbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken {

    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false,unique = true)
    private String token;
    @OneToOne
    private User user;

    private LocalDateTime expiryDate;
}
