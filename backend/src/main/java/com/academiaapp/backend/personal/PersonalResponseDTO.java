package com.academiaapp.backend.personal;

public record PersonalResponseDTO(Long id, String email, String name, String cref, String phone) {

    public static PersonalResponseDTO fromEntity(Personal personal) {
        return new PersonalResponseDTO(
                personal.getId(),
                personal.getUser().getEmail(),
                personal.getName(),
                personal.getCref(),
                personal.getPhone());
    }
}