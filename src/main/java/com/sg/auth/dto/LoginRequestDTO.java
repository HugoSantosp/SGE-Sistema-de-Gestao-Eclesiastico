package com.sg.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "Email ou CPF é obrigatório")
        String user,

        @NotBlank(message = "Senha é obrigatória")
        String senha
) {}
