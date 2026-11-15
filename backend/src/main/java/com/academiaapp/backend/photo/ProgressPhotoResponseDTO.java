package com.academiaapp.backend.photo;

import java.time.Instant;
import java.time.LocalDate;

public record ProgressPhotoResponseDTO(
        Long id,
        LocalDate takenOn,
        PhotoAngle angle,
        String notes,
        Long evaluationId,
        String contentType,
        long sizeBytes,
        Instant createdAt) {

    public static ProgressPhotoResponseDTO fromEntity(ProgressPhoto photo) {
        return new ProgressPhotoResponseDTO(
                photo.getId(),
                photo.getTakenOn(),
                photo.getAngle(),
                photo.getNotes(),
                photo.getEvaluation() == null ? null : photo.getEvaluation().getId(),
                photo.getContentType(),
                photo.getSizeBytes(),
                photo.getCreatedAt());
    }
}