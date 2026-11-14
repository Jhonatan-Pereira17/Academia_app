package com.academiaapp.backend.evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MeasureEvolutionDTO(
        String measure,
        List<Point> points,
        BigDecimal change) {

    public record Point(LocalDate date, BigDecimal value) {
    }
}