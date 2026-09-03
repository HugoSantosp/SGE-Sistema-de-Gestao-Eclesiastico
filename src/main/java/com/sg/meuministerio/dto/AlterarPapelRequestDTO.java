package com.sg.meuministerio.dto;

import com.sg.shared.enums.PapelMinisterio;
import jakarta.validation.constraints.NotNull;

/**
 * Alteração do papel (LIDER, INTEGRANTE ou papel funcional) de um membro dentro do ministério.
 */
public record AlterarPapelRequestDTO(
        @NotNull PapelMinisterio papel
) {}
