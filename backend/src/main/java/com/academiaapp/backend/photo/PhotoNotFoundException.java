package com.academiaapp.backend.photo;

public class PhotoNotFoundException extends RuntimeException {

    public PhotoNotFoundException(Long id) {
        super("Foto não encontrada: " + id);
    }
}