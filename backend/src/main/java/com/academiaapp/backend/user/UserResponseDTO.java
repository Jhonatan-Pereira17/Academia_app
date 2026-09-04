package com.academiaapp.backend.user;

public record UserResponseDTO(Long id, String email, Role role) {

    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(user.getId(), user.getEmail(), user.getRole());
    }
}