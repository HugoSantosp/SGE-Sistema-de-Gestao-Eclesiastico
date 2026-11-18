package com.sg.financeiro.dto;

import com.sg.shared.enums.TipoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
        @NotBlank @Size(max = 100) String nome,
        @NotNull TipoCategoria tipo,
        Boolean ativo
) {}
