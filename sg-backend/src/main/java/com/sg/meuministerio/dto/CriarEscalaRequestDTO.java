package com.sg.meuministerio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Criação de escala vinculada a um ministério (app MeuMinisterio).
 */
public record CriarEscalaRequestDTO(
        @NotBlank @Size(max = 120) String titulo
) {}
