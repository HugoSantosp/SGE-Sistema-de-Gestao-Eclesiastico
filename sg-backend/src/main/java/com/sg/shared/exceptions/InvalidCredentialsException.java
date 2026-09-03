package com.sg.shared.exceptions;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email/CPF ou senha inválidos");
    }
}
