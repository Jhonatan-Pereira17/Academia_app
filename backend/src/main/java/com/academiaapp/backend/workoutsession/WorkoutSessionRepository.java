package com.academiaapp.backend.workoutsession;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    Optional<WorkoutSession> findByStudentIdAndClientId(Long studentId, UUID clientId);

    List<WorkoutSession> findByStudentIdOrderByStartedAtDesc(Long studentId);
}