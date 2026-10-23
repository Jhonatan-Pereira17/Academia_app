package com.academiaapp.backend.student;

import java.time.LocalDate;

public record StudentResponseDTO(Long id, String email, String name, Long personalId,
        LocalDate birthDate, String phone) {

    public static StudentResponseDTO fromEntity(Student student) {
        return new StudentResponseDTO(
                student.getId(),
                student.getUser().getEmail(),
                student.getName(),
                student.getPersonal().getId(),
                student.getBirthDate(),
                student.getPhone());
    }
}