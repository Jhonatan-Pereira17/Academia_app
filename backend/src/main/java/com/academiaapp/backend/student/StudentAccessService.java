package com.academiaapp.backend.student;

import com.academiaapp.backend.config.BadRequestException;
import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class StudentAccessService {

    private final StudentRepository studentRepository;

    public StudentAccessService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student resolveStudent(User requester, Long requestedStudentId) {
        if (requester.getRole() == Role.STUDENT) {
            if (requestedStudentId != null && !requestedStudentId.equals(requester.getId())) {
                throw new AccessDeniedException("O aluno só pode consultar os próprios dados");
            }
            return studentRepository.findById(requester.getId())
                    .orElseThrow(() -> new IllegalStateException("Usuário STUDENT sem perfil de aluno"));
        }

        if (requestedStudentId == null) {
            throw new BadRequestException("Informe o aluno (studentId)");
        }

        Student student = studentRepository.findById(requestedStudentId)
                .orElseThrow(() -> new StudentNotFoundException(requestedStudentId));

        if (requester.getRole() == Role.PERSONAL && !student.getPersonal().getId().equals(requester.getId())) {
            throw new AccessDeniedException("Este aluno não pertence a você");
        }
        return student;
    }
}