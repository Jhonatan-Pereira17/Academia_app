package com.academiaapp.backend.workoutsession;

public class WorkoutSessionNotFoundException extends RuntimeException {

    public WorkoutSessionNotFoundException(Long id) {
        super("Sessão de treino não encontrada: " + id);
    }
}