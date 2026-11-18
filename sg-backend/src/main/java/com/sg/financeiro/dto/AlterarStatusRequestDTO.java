package com.sg.financeiro.dto;

import com.sg.shared.enums.StatusConta;
import jakarta.validation.constraints.NotNull;

/**
 * Alteração manual do status de uma conta pelo tesoureiro ou pelo bispo.
 * Cancelamento continua tendo endpoint próprio, com justificativa obrigatória.
 */
public record AlterarStatusRequestDTO(
        @NotNull StatusConta status
) {}
