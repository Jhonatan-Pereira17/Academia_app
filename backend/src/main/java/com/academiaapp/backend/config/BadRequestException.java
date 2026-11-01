package com.academiaapp.backend.config;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}