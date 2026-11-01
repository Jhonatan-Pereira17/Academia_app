package com.academiaapp.backend.workout;

public class WorkoutNotFoundException extends RuntimeException {

    public WorkoutNotFoundException(Long id) {
        super("Treino não encontrado: " + id);
    }
}