package com.sg.meuministerio.dto;

import jakarta.validation.constraints.NotBlank;

public record EscalaUpdateRequestDTO(
        @NotBlank(message = "Título é obrigatório")
        String titulo
) {}
