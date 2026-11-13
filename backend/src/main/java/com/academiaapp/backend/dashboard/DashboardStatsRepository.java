package com.academiaapp.backend.dashboard;

import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.workoutsession.WorkoutSession;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface DashboardStatsRepository extends Repository<WorkoutSession, Long> {

    @Query("select count(u) from User u where u.role = :role")
    long countUsersByRole(@Param("role") Role role);

    @Query("select count(u) from User u where u.active = :active")
    long countUsersByActive(@Param("active") boolean active);

    @Query("select count(e) from Exercise e")
    long countExercises();

    @Query("select count(e) from Exercise e where e.createdBy is null")
    long countSystemExercises();

    @Query("select count(w) from Workout w")
    long countWorkouts();

    @Query("select count(w) from Workout w where w.active = true")
    long countActiveWorkouts();

    @Query("select count(s) from WorkoutSession s where s.startedAt >= :since")
    long countSessionsSince(@Param("since") Instant since);

    @Query("select count(distinct s.student.id) from WorkoutSession s where s.startedAt >= :since")
    long countStudentsTrainedSince(@Param("since") Instant since);

    @Query("select count(w) from Workout w where w.student.personal.id = :personalId and w.active = true")
    long countActiveWorkoutsOfPersonal(@Param("personalId") Long personalId);

    @Query("select count(s) from WorkoutSession s "
            + "where s.student.personal.id = :personalId and s.startedAt >= :since")
    long countSessionsOfPersonalSince(@Param("personalId") Long personalId, @Param("since") Instant since);

    @Query("select count(distinct s.student.id) from WorkoutSession s "
            + "where s.student.personal.id = :personalId and s.startedAt >= :since")
    long countStudentsTrainedOfPersonalSince(@Param("personalId") Long personalId, @Param("since") Instant since);

    @Query("select new com.academiaapp.backend.dashboard.StudentLastSession(s.student.id, max(s.startedAt)) "
            + "from WorkoutSession s where s.student.personal.id = :personalId group by s.student.id")
    List<StudentLastSession> findLastSessionPerStudentOfPersonal(@Param("personalId") Long personalId);
}