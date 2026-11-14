package com.academiaapp.backend.evaluation;

import com.academiaapp.backend.config.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class EvaluationAnalysisService {

    private static final Map<String, Function<PhysicalEvaluationResponseDTO, BigDecimal>> MEASURES = new LinkedHashMap<>();

    static {
        MEASURES.put("weightKg", e -> e.weightKg());
        MEASURES.put("heightCm", e -> e.heightCm());
        MEASURES.put("bmi", e -> e.bmi());
        MEASURES.put("bodyFatPercentage", e -> e.bodyFatPercentage());
        MEASURES.put("muscleMassKg", e -> e.muscleMassKg());
        MEASURES.put("armCm", e -> e.armCm());
        MEASURES.put("chestCm", e -> e.chestCm());
        MEASURES.put("waistCm", e -> e.waistCm());
        MEASURES.put("hipCm", e -> e.hipCm());
        MEASURES.put("thighCm", e -> e.thighCm());
        MEASURES.put("calfCm", e -> e.calfCm());
        MEASURES.put("skinfoldTricepsMm", e -> e.skinfoldTricepsMm());
        MEASURES.put("skinfoldSubscapularMm", e -> e.skinfoldSubscapularMm());
        MEASURES.put("skinfoldChestMm", e -> e.skinfoldChestMm());
        MEASURES.put("skinfoldAxillaryMm", e -> e.skinfoldAxillaryMm());
        MEASURES.put("skinfoldSuprailiacMm", e -> e.skinfoldSuprailiacMm());
        MEASURES.put("skinfoldAbdominalMm", e -> e.skinfoldAbdominalMm());
        MEASURES.put("skinfoldThighMm", e -> e.skinfoldThighMm());
    }

    private final PhysicalEvaluationService evaluationService;

    public EvaluationAnalysisService(PhysicalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    public EvaluationComparisonDTO compare(String requesterEmail, Long fromId, Long toId) {
        PhysicalEvaluationResponseDTO from = evaluationService.findById(requesterEmail, fromId);
        PhysicalEvaluationResponseDTO to = evaluationService.findById(requesterEmail, toId);

        if (!from.studentId().equals(to.studentId())) {
            throw new BadRequestException("As avaliações são de alunos diferentes");
        }

        List<EvaluationComparisonDTO.MeasurementChange> changes = MEASURES.entrySet().stream()
                .map(entry -> change(entry.getKey(), entry.getValue().apply(from), entry.getValue().apply(to)))
                .toList();

        return new EvaluationComparisonDTO(
                from.studentId(),
                from.studentName(),
                from.id(),
                from.evaluationDate(),
                to.id(),
                to.evaluationDate(),
                ChronoUnit.DAYS.between(from.evaluationDate(), to.evaluationDate()),
                changes);
    }

    public MeasureEvolutionDTO evolution(String requesterEmail, Long studentId, String measure) {
        Function<PhysicalEvaluationResponseDTO, BigDecimal> extractor = MEASURES.get(measure);
        if (extractor == null) {
            throw new BadRequestException("Medida inválida. Use uma de: " + String.join(", ", MEASURES.keySet()));
        }

        List<PhysicalEvaluationResponseDTO> evaluations = evaluationService.list(requesterEmail, studentId);

        List<MeasureEvolutionDTO.Point> points = evaluations.stream()
                .sorted(Comparator.comparing(PhysicalEvaluationResponseDTO::evaluationDate))
                .filter(evaluation -> extractor.apply(evaluation) != null)
                .map(evaluation -> new MeasureEvolutionDTO.Point(
                        evaluation.evaluationDate(), extractor.apply(evaluation)))
                .toList();

        BigDecimal change = points.size() < 2
                ? null
                : points.get(points.size() - 1).value().subtract(points.get(0).value());

        return new MeasureEvolutionDTO(measure, points, change);
    }

    private EvaluationComparisonDTO.MeasurementChange change(String measure, BigDecimal from, BigDecimal to) {
        BigDecimal difference = (from == null || to == null) ? null : to.subtract(from);
        BigDecimal percent = (difference == null || from.signum() == 0)
                ? null
                : difference.multiply(BigDecimal.valueOf(100)).divide(from, 1, RoundingMode.HALF_UP);

        return new EvaluationComparisonDTO.MeasurementChange(measure, from, to, difference, percent);
    }
}