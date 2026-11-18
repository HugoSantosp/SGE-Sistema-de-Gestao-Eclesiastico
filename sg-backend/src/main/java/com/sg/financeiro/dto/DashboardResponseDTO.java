package com.sg.financeiro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Todos os valores chegam prontos do backend: o Angular apenas apresenta.
 */
public record DashboardResponseDTO(
        LocalDate inicio,
        LocalDate fim,
        Indicadores indicadores,
        Comparativo comparativo,
        List<EvolucaoMensal> evolucaoMensal,
        List<DistribuicaoCategoria> receitasPorCategoria,
        List<DistribuicaoCategoria> despesasPorCategoria,
        List<Movimentacao> ultimasMovimentacoes,
        List<ContaResumo> contasVencendo,
        List<ContaResumo> contasVencidas
) {

    public record Indicadores(
            BigDecimal receitasRecebidas,
            BigDecimal despesasPagas,
            BigDecimal resultado,
            BigDecimal contasPendentes,
            BigDecimal saldoInicial,
            BigDecimal saldoDisponivel
    ) {}

    /** Variacao em percentual; null quando o periodo anterior era zero. */
    public record Comparativo(
            LocalDate inicioAnterior,
            LocalDate fimAnterior,
            BigDecimal receitasRecebidas,
            BigDecimal despesasPagas,
            BigDecimal resultado,
            BigDecimal variacaoReceitas,
            BigDecimal variacaoDespesas
    ) {}

    public record EvolucaoMensal(String mes, BigDecimal receitas, BigDecimal despesas) {}

    public record DistribuicaoCategoria(Long categoriaId, String categoria, BigDecimal valor) {}

    public record Movimentacao(LocalDate data, String descricao, String tipo, BigDecimal valor, Long categoriaId) {}

    public record ContaResumo(
            Long id,
            String descricao,
            LocalDate vencimento,
            BigDecimal valor,
            BigDecimal valorPago,
            BigDecimal saldoAberto,
            String status
    ) {}
}
