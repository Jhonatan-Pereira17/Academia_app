package com.academiaapp.backend.workout;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "workout_sets")
@Getter
@Setter
@NoArgsConstructor
public class WorkoutSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    @Column(name = "set_number", nullable = false)
    private Integer setNumber;

    @Column(name = "planned_reps")
    private Integer plannedReps;

    @Column(name = "planned_load", precision = 6, scale = 2)
    private BigDecimal plannedLoad;

    @Column(name = "target_rpe")
    private Integer targetRpe;

    @Column(name = "to_failure", nullable = false)
    private boolean toFailure = false;
}