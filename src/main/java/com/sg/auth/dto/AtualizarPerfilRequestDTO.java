package com.sg.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarPerfilRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Email é obrigatório")
        String email,

        String foto
) {}
