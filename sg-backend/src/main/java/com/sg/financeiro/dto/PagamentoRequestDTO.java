package com.sg.financeiro.dto;

import com.sg.shared.enums.FormaPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pagamento de despesa. O valor pode ser menor que o saldo devedor:
 * nesse caso a conta permanece com o status em aberto e o parcial é derivado dos pagamentos.
 */
public record PagamentoRequestDTO(
        @NotNull @DecimalMin(value = "0.01") BigDecimal valorPago,
        @NotNull LocalDate dataPagamento,
        @NotNull FormaPagamento formaPagamento,
        @Size(max = 255) String observacao,
        @Size(max = 150) String arquivo
) {}
