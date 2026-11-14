package com.academiaapp.backend.evaluation;

public class PhysicalEvaluationNotFoundException extends RuntimeException {

    public PhysicalEvaluationNotFoundException(Long id) {
        super("Avaliação física não encontrada: " + id);
    }
}