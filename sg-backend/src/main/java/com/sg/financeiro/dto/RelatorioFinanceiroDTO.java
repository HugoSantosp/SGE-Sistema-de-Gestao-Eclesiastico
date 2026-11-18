package com.sg.financeiro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Relatórios financeiros. Todos os totais são apurados no backend.
 */
public record RelatorioFinanceiroDTO(
        String titulo,
        String igreja,
        LocalDate inicio,
        LocalDate fim,
        String responsavel,
        LocalDate emissao
) {

    public record ReceitaItem(
            LocalDate data,
            String descricao,
            String categoria,
            String formaPagamento,
            BigDecimal valor
    ) {}

    public record RelatorioReceitas(
            RelatorioFinanceiroDTO cabecalho,
            List<ReceitaItem> itens,
            BigDecimal total
    ) {}

    public record DespesaItem(
            String descricao,
            String categoria,
            String fornecedor,
            LocalDate vencimento,
            BigDecimal valorOriginal,
            BigDecimal valorPago,
            BigDecimal saldoPendente,
            String status
    ) {}

    public record RelatorioDespesas(
            RelatorioFinanceiroDTO cabecalho,
            List<DespesaItem> itens,
            BigDecimal totalOriginal,
            BigDecimal totalPago,
            BigDecimal totalPendente
    ) {}

    public record MovimentoCaixa(
            LocalDate data,
            String descricao,
            BigDecimal entradas,
            BigDecimal saidas,
            BigDecimal saldoAcumulado
    ) {}

    public record RelatorioFluxoCaixa(
            RelatorioFinanceiroDTO cabecalho,
            BigDecimal saldoInicial,
            BigDecimal receitasRecebidas,
            BigDecimal despesasPagas,
            List<MovimentoCaixa> movimentos,
            BigDecimal saldoFinal
    ) {}

    public record RelatorioDemonstrativo(
            RelatorioFinanceiroDTO cabecalho,
            BigDecimal totalReceitas,
            BigDecimal totalDespesasPagas,
            BigDecimal resultado,
            BigDecimal saldoInicial,
            BigDecimal saldoFinal
    ) {}
}
