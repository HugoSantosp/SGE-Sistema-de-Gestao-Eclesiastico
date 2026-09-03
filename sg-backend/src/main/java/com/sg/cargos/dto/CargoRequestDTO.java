package com.sg.cargos.dto;

import jakarta.validation.constraints.NotBlank;

public record CargoRequestDTO(
        @NotBlank(message = "Nome do cargo é obrigatório")
        String nome
) {}
