package com.sg.financeiro.dto;

import com.sg.shared.enums.FrequenciaPagamento;
import com.sg.shared.enums.StatusConta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaPagarRequestDTO(
        @NotBlank @Size(max = 100) String descricao,
        Long fornecedorId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotNull LocalDate vencimento,
        LocalDate dataEmissao,
        Long categoriaId,
        Long usuarioCadId,
        @NotNull FrequenciaPagamento frequencia,
        StatusConta status,
        @Size(max = 255) String observacao,
        @Size(max = 150) String arquivo
) {}
