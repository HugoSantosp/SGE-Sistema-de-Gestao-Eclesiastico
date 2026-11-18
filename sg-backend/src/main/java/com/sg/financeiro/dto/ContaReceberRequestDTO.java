package com.sg.financeiro.dto;

import com.sg.shared.enums.FormaPagamento;
import com.sg.shared.enums.FrequenciaPagamento;
import com.sg.shared.enums.StatusConta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaReceberRequestDTO(
        @NotBlank @Size(max = 100) String descricao,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotNull LocalDate vencimento,
        @NotNull FrequenciaPagamento frequencia,
        StatusConta status,
        @Size(max = 150) String contribuinte,
        Long categoriaId,
        FormaPagamento formaPagamento,
        @Size(max = 255) String observacao
) {}
