package com.sg.escala.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EscalaRequestDTO(
        @NotBlank @Size(max = 120) String titulo,
        Long ministerioId
) {}
