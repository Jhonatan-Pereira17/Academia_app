package com.academiaapp.backend.workoutsession;

import com.academiaapp.backend.student.Student;
import com.academiaapp.backend.workout.Workout;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workout_sessions")
@Getter
@Setter
@NoArgsConstructor
public class WorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, updatable = false)
    private UUID clientId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    private Workout workout;

    @Column(name = "workout_name", nullable = false)
    private String workoutName;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("exerciseOrder ASC, setNumber ASC")
    private List<ExerciseLog> logs = new ArrayList<>();
}