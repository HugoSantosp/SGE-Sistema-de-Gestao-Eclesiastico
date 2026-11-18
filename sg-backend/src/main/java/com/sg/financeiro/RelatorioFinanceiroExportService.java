package com.sg.financeiro;

import com.sg.financeiro.dto.RelatorioFinanceiroDTO;
import com.sg.shared.services.ReportService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converte os relatórios já apurados em PDF e Excel. Nenhum cálculo financeiro
 * acontece aqui: apenas formatação do que o RelatorioFinanceiroService produziu.
 */
@Service
public class RelatorioFinanceiroExportService {

    private final RelatorioFinanceiroService relatorioService;
    private final ReportService reportService;

    public RelatorioFinanceiroExportService(RelatorioFinanceiroService relatorioService,
                                            ReportService reportService) {
        this.relatorioService = relatorioService;
        this.reportService = reportService;
    }

    public byte[] receitasPdf(LocalDate inicio, LocalDate fim) throws IOException {
        return reportService.generateRelatorioReceitasPdf(relatorioService.receitas(inicio, fim));
    }

    public byte[] despesasPdf(LocalDate inicio, LocalDate fim) throws IOException {
        return reportService.generateRelatorioDespesasPdf(relatorioService.despesas(inicio, fim));
    }

    public byte[] fluxoCaixaPdf(LocalDate inicio, LocalDate fim) throws IOException {
        return reportService.generateRelatorioFluxoCaixaPdf(relatorioService.fluxoCaixa(inicio, fim));
    }

    public byte[] demonstrativoPdf(LocalDate inicio, LocalDate fim) throws IOException {
        return reportService.generateRelatorioDemonstrativoPdf(relatorioService.demonstrativo(inicio, fim));
    }

    public byte[] receitasExcel(LocalDate inicio, LocalDate fim) throws IOException {
        RelatorioFinanceiroDTO.RelatorioReceitas relatorio = relatorioService.receitas(inicio, fim);

        List<String> headers = List.of("Data", "Descrição", "Categoria", "Forma de pagamento", "Valor");
        List<Map<String, Object>> linhas = new ArrayList<>();
        for (RelatorioFinanceiroDTO.ReceitaItem item : relatorio.itens()) {
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("Data", item.data());
            linha.put("Descrição", item.descricao());
            linha.put("Categoria", item.categoria());
            linha.put("Forma de pagamento", item.formaPagamento());
            linha.put("Valor", item.valor());
            linhas.add(linha);
        }

        Map<String, BigDecimal> totais = new LinkedHashMap<>();
        totais.put("Valor", relatorio.total());

        return reportService.generateRelatorioFinanceiroExcel("Receitas", headers, linhas, totais);
    }

    public byte[] despesasExcel(LocalDate inicio, LocalDate fim) throws IOException {
        RelatorioFinanceiroDTO.RelatorioDespesas relatorio = relatorioService.despesas(inicio, fim);

        List<String> headers = List.of("Descrição", "Categoria", "Fornecedor", "Vencimento",
                "Valor original", "Valor pago", "Saldo pendente", "Status");
        List<Map<String, Object>> linhas = new ArrayList<>();
        for (RelatorioFinanceiroDTO.DespesaItem item : relatorio.itens()) {
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("Descrição", item.descricao());
            linha.put("Categoria", item.categoria());
            linha.put("Fornecedor", item.fornecedor());
            linha.put("Vencimento", item.vencimento());
            linha.put("Valor original", item.valorOriginal());
            linha.put("Valor pago", item.valorPago());
            linha.put("Saldo pendente", item.saldoPendente());
            linha.put("Status", item.status());
            linhas.add(linha);
        }

        Map<String, BigDecimal> totais = new LinkedHashMap<>();
        totais.put("Valor original", relatorio.totalOriginal());
        totais.put("Valor pago", relatorio.totalPago());
        totais.put("Saldo pendente", relatorio.totalPendente());

        return reportService.generateRelatorioFinanceiroExcel("Despesas", headers, linhas, totais);
    }

    public byte[] fluxoCaixaExcel(LocalDate inicio, LocalDate fim) throws IOException {
        RelatorioFinanceiroDTO.RelatorioFluxoCaixa relatorio = relatorioService.fluxoCaixa(inicio, fim);

        List<String> headers = List.of("Data", "Descrição", "Entradas", "Saídas", "Saldo acumulado");
        List<Map<String, Object>> linhas = new ArrayList<>();
        for (RelatorioFinanceiroDTO.MovimentoCaixa m : relatorio.movimentos()) {
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("Data", m.data());
            linha.put("Descrição", m.descricao());
            linha.put("Entradas", m.entradas());
            linha.put("Saídas", m.saidas());
            linha.put("Saldo acumulado", m.saldoAcumulado());
            linhas.add(linha);
        }

        Map<String, BigDecimal> totais = new LinkedHashMap<>();
        totais.put("Entradas", relatorio.receitasRecebidas());
        totais.put("Saídas", relatorio.despesasPagas());
        totais.put("Saldo acumulado", relatorio.saldoFinal());

        return reportService.generateRelatorioFinanceiroExcel("Fluxo de Caixa", headers, linhas, totais);
    }

    public byte[] demonstrativoExcel(LocalDate inicio, LocalDate fim) throws IOException {
        RelatorioFinanceiroDTO.RelatorioDemonstrativo relatorio = relatorioService.demonstrativo(inicio, fim);

        List<String> headers = List.of("Indicador", "Valor");
        List<Map<String, Object>> linhas = new ArrayList<>();

        linhas.add(linha("Total de receitas", relatorio.totalReceitas()));
        linhas.add(linha("Total de despesas pagas", relatorio.totalDespesasPagas()));
        linhas.add(linha("Resultado financeiro", relatorio.resultado()));
        linhas.add(linha("Saldo inicial", relatorio.saldoInicial()));
        linhas.add(linha("Saldo final", relatorio.saldoFinal()));

        return reportService.generateRelatorioFinanceiroExcel("Demonstrativo", headers, linhas, null);
    }

    private Map<String, Object> linha(String indicador, BigDecimal valor) {
        Map<String, Object> linha = new LinkedHashMap<>();
        linha.put("Indicador", indicador);
        linha.put("Valor", valor);
        return linha;
    }
}
