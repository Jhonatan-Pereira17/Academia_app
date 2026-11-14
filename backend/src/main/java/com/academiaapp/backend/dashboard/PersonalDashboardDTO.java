package com.academiaapp.backend.dashboard;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PersonalDashboardDTO(
                Long personalId,
                String personalName,
                int totalStudents,
                long activeWorkouts,
                PeriodActivityDTO last7Days,
                PeriodActivityDTO last30Days,
                int inactiveDaysThreshold,
                List<InactiveStudent> inactiveStudents,
                int pendingEvaluationDaysThreshold,
                List<PendingEvaluation> pendingEvaluations) {

        public record InactiveStudent(
                        Long studentId,
                        String name,
                        Instant lastSessionAt,
                        Long daysSinceLastSession) {
        }

        public record PendingEvaluation(
                        Long studentId,
                        String name,
                        LocalDate lastEvaluationDate,
                        Long daysSinceLastEvaluation) {
        }
}