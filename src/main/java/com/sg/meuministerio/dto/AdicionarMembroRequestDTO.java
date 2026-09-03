package com.sg.meuministerio.dto;

import com.sg.shared.enums.PapelMinisterio;
import jakarta.validation.constraints.NotNull;

/**
 * Adiciona um membro existente a um ministério com um papel (LIDER, INTEGRANTE ou papel funcional).
 */
public record AdicionarMembroRequestDTO(
        @NotNull Long membroId,
        @NotNull PapelMinisterio papel
) {}
