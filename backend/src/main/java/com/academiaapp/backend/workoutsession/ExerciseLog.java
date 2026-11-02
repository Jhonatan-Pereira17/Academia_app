package com.academiaapp.backend.workoutsession;

import com.academiaapp.backend.exercise.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exercise_logs")
@Getter
@Setter
@NoArgsConstructor
public class ExerciseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, updatable = false)
    private UUID clientId;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private WorkoutSession session;

    @ManyToOne
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "exercise_order", nullable = false)
    private Integer exerciseOrder;

    @Column(name = "set_number", nullable = false)
    private Integer setNumber;

    @Column(nullable = false)
    private Integer reps;

    @Column(name = "load_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal loadKg;

    private Integer rpe;

    @Column(name = "to_failure", nullable = false)
    private boolean toFailure = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;
}