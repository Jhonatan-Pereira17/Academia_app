package com.academiaapp.backend.personal;

import com.academiaapp.backend.user.EmailAlreadyInUseException;
import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PersonalService {

    private final UserRepository userRepository;
    private final PersonalRepository personalRepository;
    private final PasswordEncoder passwordEncoder;

    public PersonalService(UserRepository userRepository,
            PersonalRepository personalRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.personalRepository = personalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PersonalResponseDTO create(CreatePersonalRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyInUseException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.PERSONAL);
        userRepository.save(user);

        Personal personal = new Personal();
        personal.setUser(user);
        personal.setName(request.name());
        personal.setCref(request.cref());
        personal.setPhone(request.phone());
        personalRepository.save(personal);

        return PersonalResponseDTO.fromEntity(personal);
    }

    @Transactional(readOnly = true)
    public List<PersonalResponseDTO> findAll() {
        return personalRepository.findAll().stream()
                .map(PersonalResponseDTO::fromEntity)
                .toList();
    }
}