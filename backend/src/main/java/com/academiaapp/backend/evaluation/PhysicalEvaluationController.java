package com.academiaapp.backend.evaluation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/physical-evaluations")
public class PhysicalEvaluationController {

    private final PhysicalEvaluationService evaluationService;

    public PhysicalEvaluationController(PhysicalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PERSONAL')")
    public ResponseEntity<PhysicalEvaluationResponseDTO> create(Authentication authentication,
            @Valid @RequestBody CreatePhysicalEvaluationRequest request) {
        PhysicalEvaluationResponseDTO created = evaluationService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<PhysicalEvaluationResponseDTO> list(Authentication authentication,
            @RequestParam(name = "studentId", required = false) Long studentId) {
        return evaluationService.list(authentication.getName(), studentId);
    }

    @GetMapping("/{id}")
    public PhysicalEvaluationResponseDTO findById(Authentication authentication, @PathVariable("id") Long id) {
        return evaluationService.findById(authentication.getName(), id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PERSONAL')")
    public PhysicalEvaluationResponseDTO update(Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody CreatePhysicalEvaluationRequest request) {
        return evaluationService.update(authentication.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PERSONAL')")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable("id") Long id) {
        evaluationService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}