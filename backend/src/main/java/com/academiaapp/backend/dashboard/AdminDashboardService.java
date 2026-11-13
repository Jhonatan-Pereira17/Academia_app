package com.academiaapp.backend.dashboard;

import com.academiaapp.backend.user.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AdminDashboardService {

    private final DashboardStatsRepository stats;

    public AdminDashboardService(DashboardStatsRepository stats) {
        this.stats = stats;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDTO adminDashboard() {
        Instant now = Instant.now();

        long active = stats.countUsersByActive(true);
        long blocked = stats.countUsersByActive(false);
        AdminDashboardDTO.Users users = new AdminDashboardDTO.Users(
                active + blocked,
                active,
                blocked,
                stats.countUsersByRole(Role.ADMIN),
                stats.countUsersByRole(Role.PERSONAL),
                stats.countUsersByRole(Role.STUDENT));

        long exercises = stats.countExercises();
        long systemExercises = stats.countSystemExercises();
        AdminDashboardDTO.Content content = new AdminDashboardDTO.Content(
                exercises,
                systemExercises,
                exercises - systemExercises,
                stats.countWorkouts(),
                stats.countActiveWorkouts());

        return new AdminDashboardDTO(
                users,
                content,
                activity(now.minus(7, ChronoUnit.DAYS)),
                activity(now.minus(30, ChronoUnit.DAYS)));
    }

    private PeriodActivityDTO activity(Instant since) {
        return new PeriodActivityDTO(
                stats.countSessionsSince(since),
                stats.countStudentsTrainedSince(since));
    }
}