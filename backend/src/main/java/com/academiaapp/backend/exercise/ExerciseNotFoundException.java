package com.academiaapp.backend.exercise;

public class ExerciseNotFoundException extends RuntimeException {

    public ExerciseNotFoundException(Long id) {
        super("Exercício não encontrado: " + id);
    }
}