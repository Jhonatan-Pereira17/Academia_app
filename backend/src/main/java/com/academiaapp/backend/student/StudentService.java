package com.academiaapp.backend.student;

import com.academiaapp.backend.personal.Personal;
import com.academiaapp.backend.personal.PersonalRepository;
import com.academiaapp.backend.user.EmailAlreadyInUseException;
import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final PersonalRepository personalRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
            PersonalRepository personalRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.personalRepository = personalRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public StudentResponseDTO create(String personalEmail, CreateStudentRequest request) {
        User personalUser = findUserByEmail(personalEmail);
        Personal personal = personalRepository.findById(personalUser.getId())
                .orElseThrow(() -> new IllegalStateException("Usuário PERSONAL sem perfil de personal"));

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyInUseException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.STUDENT);
        userRepository.save(user);

        Student student = new Student();
        student.setUser(user);
        student.setName(request.name());
        student.setPersonal(personal);
        student.setBirthDate(request.birthDate());
        student.setPhone(request.phone());
        studentRepository.save(student);

        return StudentResponseDTO.fromEntity(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> list(String requesterEmail) {
        User requester = findUserByEmail(requesterEmail);

        List<Student> students = requester.getRole() == Role.ADMIN
                ? studentRepository.findAll()
                : studentRepository.findByPersonalId(requester.getId());

        return students.stream()
                .map(StudentResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponseDTO findById(String requesterEmail, Long studentId) {
        User requester = findUserByEmail(requesterEmail);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        boolean isAdmin = requester.getRole() == Role.ADMIN;
        boolean isOwnerPersonal = requester.getRole() == Role.PERSONAL
                && student.getPersonal().getId().equals(requester.getId());

        if (!isAdmin && !isOwnerPersonal) {
            throw new AccessDeniedException("Acesso negado a este aluno");
        }

        return StudentResponseDTO.fromEntity(student);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }
}