package com.academiaapp.backend.evaluation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePhysicalEvaluationRequest(
        @NotNull(message = "O aluno (studentId) é obrigatório") Long studentId,

        @NotNull(message = "A data da avaliação é obrigatória") @PastOrPresent(message = "A data da avaliação não pode estar no futuro") LocalDate evaluationDate,

        @DecimalMin(value = "20.0", message = "O peso deve estar entre 20 e 400 kg") @DecimalMax(value = "400.0", message = "O peso deve estar entre 20 e 400 kg") BigDecimal weightKg,

        @DecimalMin(value = "50.0", message = "A altura deve estar entre 50 e 260 cm") @DecimalMax(value = "260.0", message = "A altura deve estar entre 50 e 260 cm") BigDecimal heightCm,

        @DecimalMin(value = "1.0", message = "O percentual de gordura deve estar entre 1 e 75") @DecimalMax(value = "75.0", message = "O percentual de gordura deve estar entre 1 e 75") BigDecimal bodyFatPercentage,

        @DecimalMin(value = "1.0", message = "A massa muscular deve estar entre 1 e 200 kg") @DecimalMax(value = "200.0", message = "A massa muscular deve estar entre 1 e 200 kg") BigDecimal muscleMassKg,

        @DecimalMin(value = "5.0", message = "A circunferência do braço deve estar entre 5 e 300 cm") @DecimalMax(value = "300.0", message = "A circunferência do braço deve estar entre 5 e 300 cm") BigDecimal armCm,

        @DecimalMin(value = "5.0", message = "A circunferência do peito deve estar entre 5 e 300 cm") @DecimalMax(value = "300.0", message = "A circunferência do peito deve estar entre 5 e 300 cm") BigDecimal chestCm,

        @DecimalMin(value = "5.0", message = "A circunferência da cintura deve estar entre 5 e 300 cm") @DecimalMax(value = "300.0", message = "A circunferência da cintura deve estar entre 5 e 300 cm") BigDecimal waistCm,

        @DecimalMin(value = "5.0", message = "A circunferência do quadril deve estar entre 5 e 300 cm") @DecimalMax(value = "300.0", message = "A circunferência do quadril deve estar entre 5 e 300 cm") BigDecimal hipCm,

        @DecimalMin(value = "5.0", message = "A circunferência da coxa deve estar entre 5 e 300 cm") @DecimalMax(value = "300.0", message = "A circunferência da coxa deve estar entre 5 e 300 cm") BigDecimal thighCm,

        @DecimalMin(value = "5.0", message = "A circunferência da panturrilha deve estar entre 5 e 300 cm") @DecimalMax(value = "300.0", message = "A circunferência da panturrilha deve estar entre 5 e 300 cm") BigDecimal calfCm,

        @DecimalMin(value = "1.0", message = "A dobra do tríceps deve estar entre 1 e 100 mm") @DecimalMax(value = "100.0", message = "A dobra do tríceps deve estar entre 1 e 100 mm") BigDecimal skinfoldTricepsMm,

        @DecimalMin(value = "1.0", message = "A dobra subescapular deve estar entre 1 e 100 mm") @DecimalMax(value = "100.0", message = "A dobra subescapular deve estar entre 1 e 100 mm") BigDecimal skinfoldSubscapularMm,

        @DecimalMin(value = "1.0", message = "A dobra peitoral deve estar entre 1 e 100 mm") @DecimalMax(value = "100.0", message = "A dobra peitoral deve estar entre 1 e 100 mm") BigDecimal skinfoldChestMm,

        @DecimalMin(value = "1.0", message = "A dobra axilar deve estar entre 1 e 100 mm") @DecimalMax(value = "100.0", message = "A dobra axilar deve estar entre 1 e 100 mm") BigDecimal skinfoldAxillaryMm,

        @DecimalMin(value = "1.0", message = "A dobra suprailíaca deve estar entre 1 e 100 mm") @DecimalMax(value = "100.0", message = "A dobra suprailíaca deve estar entre 1 e 100 mm") BigDecimal skinfoldSuprailiacMm,

        @DecimalMin(value = "1.0", message = "A dobra abdominal deve estar entre 1 e 100 mm") @DecimalMax(value = "100.0", message = "A dobra abdominal deve estar entre 1 e 100 mm") BigDecimal skinfoldAbdominalMm,

        @DecimalMin(value = "1.0", message = "A dobra da coxa deve estar entre 1 e 100 mm") @DecimalMax(value = "100.0", message = "A dobra da coxa deve estar entre 1 e 100 mm") BigDecimal skinfoldThighMm,

        String notes) {
}