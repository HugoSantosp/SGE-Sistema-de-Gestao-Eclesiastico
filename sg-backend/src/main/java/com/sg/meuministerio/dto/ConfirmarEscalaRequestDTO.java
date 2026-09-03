package com.sg.meuministerio.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Confirmação de disponibilidade do membro logado (app MeuMinisterio).
 * dataIds vazio = declina todas as datas.
 */
public record ConfirmarEscalaRequestDTO(
        @NotEmpty List<Long> dataIds
) {}
