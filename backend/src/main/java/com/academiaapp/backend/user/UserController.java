package com.academiaapp.backend.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<UserResponseDTO> listUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }
}