package com.academiaapp.backend.workoutsession;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

        Optional<WorkoutSession> findByStudentIdAndClientId(Long studentId, UUID clientId);

        List<WorkoutSession> findByStudentIdOrderByStartedAtDesc(Long studentId, Pageable pageable);

        List<WorkoutSession> findByStudentPersonalIdOrderByStartedAtDesc(Long personalId, Pageable pageable);

        List<WorkoutSession> findAllByOrderByStartedAtDesc(Pageable pageable);

        List<WorkoutSession> findByStudentIdAndStartedAtGreaterThanEqualOrderByStartedAtDesc(Long studentId,
                        Instant since);

        List<WorkoutSession> findByStudentIdAndWorkoutIsNotNullOrderByStartedAtDesc(Long studentId,
                        Pageable pageable);

        @Query("select s from WorkoutSession s "
                        + "where s.student.id = :studentId "
                        + "and exists (select l.id from ExerciseLog l where l.session = s and l.exercise.id = :exerciseId) "
                        + "order by s.startedAt desc")
        List<WorkoutSession> findSessionsWithExercise(@Param("studentId") Long studentId,
                        @Param("exerciseId") Long exerciseId,
                        Pageable pageable);

        @Query("select s.startedAt from WorkoutSession s "
                        + "where s.student.id = :studentId and s.startedAt >= :since "
                        + "order by s.startedAt desc")
        List<Instant> findStartTimesSince(@Param("studentId") Long studentId, @Param("since") Instant since);
}