package com.sg.secretarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record SecretarioRequestDTO(
        @NotBlank String nome,
        @NotBlank @Email String email,
        String documento, String telefone, String endereco,
        String foto, LocalDate dataNasc
) {}
