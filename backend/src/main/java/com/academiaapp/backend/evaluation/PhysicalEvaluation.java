package com.academiaapp.backend.evaluation;

import com.academiaapp.backend.personal.Personal;
import com.academiaapp.backend.student.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "physical_evaluations")
@Getter
@Setter
@NoArgsConstructor
public class PhysicalEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @Column(name = "evaluation_date", nullable = false)
    private LocalDate evaluationDate;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "body_fat_percentage", precision = 4, scale = 1)
    private BigDecimal bodyFatPercentage;

    @Column(name = "muscle_mass_kg", precision = 5, scale = 2)
    private BigDecimal muscleMassKg;

    @Column(name = "arm_cm", precision = 5, scale = 1)
    private BigDecimal armCm;

    @Column(name = "chest_cm", precision = 5, scale = 1)
    private BigDecimal chestCm;

    @Column(name = "waist_cm", precision = 5, scale = 1)
    private BigDecimal waistCm;

    @Column(name = "hip_cm", precision = 5, scale = 1)
    private BigDecimal hipCm;

    @Column(name = "thigh_cm", precision = 5, scale = 1)
    private BigDecimal thighCm;

    @Column(name = "calf_cm", precision = 5, scale = 1)
    private BigDecimal calfCm;

    @Column(name = "skinfold_triceps_mm", precision = 4, scale = 1)
    private BigDecimal skinfoldTricepsMm;

    @Column(name = "skinfold_subscapular_mm", precision = 4, scale = 1)
    private BigDecimal skinfoldSubscapularMm;

    @Column(name = "skinfold_chest_mm", precision = 4, scale = 1)
    private BigDecimal skinfoldChestMm;

    @Column(name = "skinfold_axillary_mm", precision = 4, scale = 1)
    private BigDecimal skinfoldAxillaryMm;

    @Column(name = "skinfold_suprailiac_mm", precision = 4, scale = 1)
    private BigDecimal skinfoldSuprailiacMm;

    @Column(name = "skinfold_abdominal_mm", precision = 4, scale = 1)
    private BigDecimal skinfoldAbdominalMm;

    @Column(name = "skinfold_thigh_mm", precision = 4, scale = 1)
    private BigDecimal skinfoldThighMm;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}