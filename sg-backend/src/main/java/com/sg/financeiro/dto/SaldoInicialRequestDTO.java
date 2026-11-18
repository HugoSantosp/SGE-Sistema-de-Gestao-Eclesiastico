package com.sg.financeiro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaldoInicialRequestDTO(
        @NotNull BigDecimal valor,
        @NotNull LocalDate dataReferencia,
        @Size(max = 255) String observacao
) {}
