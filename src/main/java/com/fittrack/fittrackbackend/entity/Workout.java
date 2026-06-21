package com.fittrack.fittrackbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="workouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workout {
@Id
@GeneratedValue
private UUID id;

@Column
private String title;

@Column
private Integer durationMinutes;

@Column
private Double caloriesBurned;

@Column
private LocalDate workoutDate;

@ManyToOne
@JoinColumn(name="user_id")
private User user;


}
