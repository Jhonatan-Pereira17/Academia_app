package com.academiaapp.backend.evaluation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

public record PhysicalEvaluationResponseDTO(
        Long id,
        Long studentId,
        String studentName,
        Long personalId,
        String personalName,
        LocalDate evaluationDate,
        BigDecimal weightKg,
        BigDecimal heightCm,
        BigDecimal bmi,
        BigDecimal bodyFatPercentage,
        BigDecimal muscleMassKg,
        BigDecimal armCm,
        BigDecimal chestCm,
        BigDecimal waistCm,
        BigDecimal hipCm,
        BigDecimal thighCm,
        BigDecimal calfCm,
        BigDecimal skinfoldTricepsMm,
        BigDecimal skinfoldSubscapularMm,
        BigDecimal skinfoldChestMm,
        BigDecimal skinfoldAxillaryMm,
        BigDecimal skinfoldSuprailiacMm,
        BigDecimal skinfoldAbdominalMm,
        BigDecimal skinfoldThighMm,
        String notes,
        Instant createdAt) {

    public static PhysicalEvaluationResponseDTO fromEntity(PhysicalEvaluation e) {
        return new PhysicalEvaluationResponseDTO(
                e.getId(),
                e.getStudent().getId(),
                e.getStudent().getName(),
                e.getPersonal().getId(),
                e.getPersonal().getName(),
                e.getEvaluationDate(),
                e.getWeightKg(),
                e.getHeightCm(),
                bmi(e.getWeightKg(), e.getHeightCm()),
                e.getBodyFatPercentage(),
                e.getMuscleMassKg(),
                e.getArmCm(),
                e.getChestCm(),
                e.getWaistCm(),
                e.getHipCm(),
                e.getThighCm(),
                e.getCalfCm(),
                e.getSkinfoldTricepsMm(),
                e.getSkinfoldSubscapularMm(),
                e.getSkinfoldChestMm(),
                e.getSkinfoldAxillaryMm(),
                e.getSkinfoldSuprailiacMm(),
                e.getSkinfoldAbdominalMm(),
                e.getSkinfoldThighMm(),
                e.getNotes(),
                e.getCreatedAt());
    }

    static BigDecimal bmi(BigDecimal weightKg, BigDecimal heightCm) {
        if (weightKg == null || heightCm == null) {
            return null;
        }
        BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return weightKg.divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP);
    }
}