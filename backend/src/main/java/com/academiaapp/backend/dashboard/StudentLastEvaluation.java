package com.academiaapp.backend.dashboard;

import java.time.LocalDate;

public record StudentLastEvaluation(Long studentId, LocalDate lastEvaluationDate) {
}