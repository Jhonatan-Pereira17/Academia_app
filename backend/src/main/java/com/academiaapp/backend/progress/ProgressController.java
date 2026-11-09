package com.academiaapp.backend.progress;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/workout-sessions")
    public List<WorkoutSessionSummaryDTO> listSessions(Authentication authentication,
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return progressService.listSessions(authentication.getName(), studentId, page, size);
    }

    @GetMapping("/progress/exercises/{exerciseId}/last")
    public ResponseEntity<LastPerformanceDTO> lastPerformance(Authentication authentication,
            @PathVariable("exerciseId") Long exerciseId,
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "excludeSessionId", required = false) Long excludeSessionId) {
        return progressService.lastPerformance(authentication.getName(), exerciseId, studentId, excludeSessionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/progress/exercises/{exerciseId}/history")
    public List<ExerciseHistoryItemDTO> exerciseHistory(Authentication authentication,
            @PathVariable("exerciseId") Long exerciseId,
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return progressService.exerciseHistory(authentication.getName(), exerciseId, studentId, limit);
    }
}