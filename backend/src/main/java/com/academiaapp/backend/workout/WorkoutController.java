package com.academiaapp.backend.workout;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PERSONAL', 'STUDENT')")
    public ResponseEntity<WorkoutResponseDTO> create(Authentication authentication,
            @Valid @RequestBody CreateWorkoutRequest request) {
        WorkoutResponseDTO created = workoutService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<WorkoutResponseDTO> list(Authentication authentication,
            @RequestParam(name = "includeArchived", defaultValue = "false") boolean includeArchived) {
        return workoutService.list(authentication.getName(), includeArchived);
    }

    @GetMapping("/{id}")
    public WorkoutResponseDTO findById(Authentication authentication, @PathVariable("id") Long id) {
        return workoutService.findById(authentication.getName(), id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PERSONAL', 'STUDENT')")
    public WorkoutResponseDTO update(Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateWorkoutRequest request) {
        return workoutService.update(authentication.getName(), id, request);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('PERSONAL', 'STUDENT')")
    public WorkoutResponseDTO setActive(Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateWorkoutActiveRequest request) {
        return workoutService.setActive(authentication.getName(), id, request.active());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PERSONAL', 'STUDENT')")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable("id") Long id) {
        workoutService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}