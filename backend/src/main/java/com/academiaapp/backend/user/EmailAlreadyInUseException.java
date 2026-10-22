package com.academiaapp.backend.user;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("Já existe um usuário cadastrado com o email: " + email);
    }
}