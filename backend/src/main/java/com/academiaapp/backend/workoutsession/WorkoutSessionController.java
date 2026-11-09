package com.academiaapp.backend.workoutsession;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workout-sessions")
public class WorkoutSessionController {

    private final WorkoutSessionService sessionService;

    public WorkoutSessionController(WorkoutSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<WorkoutSessionResponseDTO> create(Authentication authentication,
            @Valid @RequestBody CreateWorkoutSessionRequest request) {
        WorkoutSessionService.SaveResult result = sessionService.create(authentication.getName(), request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.session());
    }

    @PostMapping("/{id}/logs")
    @PreAuthorize("hasRole('STUDENT')")
    public WorkoutSessionResponseDTO addLogs(Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody AddLogsRequest request) {
        return sessionService.addLogs(authentication.getName(), id, request.logs());
    }

    @PatchMapping("/{id}/finish")
    @PreAuthorize("hasRole('STUDENT')")
    public WorkoutSessionResponseDTO finish(Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody FinishWorkoutSessionRequest request) {
        return sessionService.finish(authentication.getName(), id, request);
    }

    @GetMapping("/{id}")
    public WorkoutSessionResponseDTO findById(Authentication authentication, @PathVariable("id") Long id) {
        return sessionService.findById(authentication.getName(), id);
    }
}