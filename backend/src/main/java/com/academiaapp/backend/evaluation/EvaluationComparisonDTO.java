package com.academiaapp.backend.evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EvaluationComparisonDTO(
        Long studentId,
        String studentName,
        Long fromId,
        LocalDate fromDate,
        Long toId,
        LocalDate toDate,
        long daysBetween,
        List<MeasurementChange> changes) {

    public record MeasurementChange(
            String measure,
            BigDecimal from,
            BigDecimal to,
            BigDecimal change,
            BigDecimal changePercent) {
    }
}