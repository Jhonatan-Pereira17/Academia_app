package com.academiaapp.backend.evaluation;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/physical-evaluations")
public class EvaluationAnalysisController {

    private final EvaluationAnalysisService analysisService;

    public EvaluationAnalysisController(EvaluationAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/compare")
    public EvaluationComparisonDTO compare(Authentication authentication,
            @RequestParam(name = "fromId") Long fromId,
            @RequestParam(name = "toId") Long toId) {
        return analysisService.compare(authentication.getName(), fromId, toId);
    }

    @GetMapping("/evolution")
    public MeasureEvolutionDTO evolution(Authentication authentication,
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "measure") String measure) {
        return analysisService.evolution(authentication.getName(), studentId, measure);
    }
}