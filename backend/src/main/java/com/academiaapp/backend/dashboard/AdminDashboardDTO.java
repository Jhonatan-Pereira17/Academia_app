package com.academiaapp.backend.dashboard;

public record AdminDashboardDTO(
        Users users,
        Content content,
        PeriodActivityDTO last7Days,
        PeriodActivityDTO last30Days) {

    public record Users(
            long total,
            long active,
            long blocked,
            long admins,
            long personals,
            long students) {
    }

    public record Content(
            long exercises,
            long systemExercises,
            long customExercises,
            long workouts,
            long activeWorkouts) {
    }
}