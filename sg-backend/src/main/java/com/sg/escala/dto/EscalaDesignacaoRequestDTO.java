package com.sg.escala.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EscalaDesignacaoRequestDTO(
        @NotNull Long confirmacaoId,
        @NotBlank String instrumento,
        Integer ordem
) {}
