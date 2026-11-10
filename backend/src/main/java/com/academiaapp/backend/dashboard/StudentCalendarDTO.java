package com.academiaapp.backend.dashboard;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record StudentCalendarDTO(
        String month,
        int daysInMonth,
        int daysTrained,
        int totalSessions,
        List<TrainedDay> trainedDays) {

    public record TrainedDay(LocalDate date, List<SessionItem> sessions) {
    }

    public record SessionItem(
            Long id,
            String workoutName,
            Instant startedAt,
            Long durationMinutes) {
    }
}