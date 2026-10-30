package com.academiaapp.backend.exercise;

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
@RequestMapping("/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public List<ExerciseResponseDTO> list(
            @RequestParam(name = "muscleGroup", required = false) MuscleGroup muscleGroup) {
        return exerciseService.list(muscleGroup);
    }

    @GetMapping("/{id}")
    public ExerciseResponseDTO findById(@PathVariable("id") Long id) {
        return exerciseService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PERSONAL')")
    public ResponseEntity<ExerciseResponseDTO> create(Authentication authentication,
            @Valid @RequestBody CreateExerciseRequest request) {
        ExerciseResponseDTO created = exerciseService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PERSONAL')")
    public ExerciseResponseDTO update(Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateExerciseRequest request) {
        return exerciseService.update(authentication.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PERSONAL')")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable("id") Long id) {
        exerciseService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}