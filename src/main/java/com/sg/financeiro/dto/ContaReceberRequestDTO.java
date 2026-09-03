package com.sg.financeiro.dto;

import com.sg.shared.enums.FrequenciaPagamento;
import com.sg.shared.enums.StatusConta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaReceberRequestDTO(
        @NotBlank String descricao,
        @NotNull BigDecimal valor,
        @NotNull LocalDate vencimento,
        @NotNull FrequenciaPagamento frequencia,
        StatusConta status,
        String contribuinte
) {}
