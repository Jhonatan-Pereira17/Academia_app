package com.academiaapp.backend.student;

public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(Long id) {
        super("Aluno não encontrado: " + id);
    }
}