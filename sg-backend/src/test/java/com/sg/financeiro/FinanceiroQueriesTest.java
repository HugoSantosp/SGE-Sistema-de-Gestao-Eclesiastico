package com.sg.financeiro;

import com.sg.shared.enums.FormaPagamento;
import com.sg.shared.enums.FrequenciaPagamento;
import com.sg.shared.enums.StatusConta;
import com.sg.shared.enums.StatusDoacao;
import com.sg.shared.enums.TipoDoacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Valida a sintaxe e o resultado das consultas JPQL dos repositórios financeiros
 * contra um banco real (H2). Sem este teste as queries só falhariam em runtime.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Financeiro - Consultas JPQL")
class FinanceiroQueriesTest {

    @Autowired private ContaPagarRepository contaPagarRepository;
    @Autowired private ContaReceberRepository contaReceberRepository;
    @Autowired private PagamentoDespesaRepository pagamentoDespesaRepository;
    @Autowired private DoacaoRepository doacaoRepository;
    @Autowired private CategoriaFinanceiraRepository categoriaRepository;
    @Autowired private SaldoInicialRepository saldoInicialRepository;

    private final LocalDate inicio = LocalDate.of(2026, 11, 1);
    private final LocalDate fim = LocalDate.of(2026, 11, 30);

    private ContaPagar contaComPagamento;
    private ContaPagar contaBaixaDireta;
    private ContaPagar contaPendente;

    @BeforeEach
    void setUp() {
        CategoriaFinanceira categoria = categoriaRepository.save(CategoriaFinanceira.builder()
                .nome("Dízimos").tipo(com.sg.shared.enums.TipoCategoria.RECEITA)
                .ativo(true).dataCad(LocalDate.now()).build());

        doacaoRepository.save(Doacao.builder()
                .tipo(TipoDoacao.DIZIMO).nomeDoador("Irmã Maria")
                .dataDoacao(LocalDate.of(2026, 11, 10)).formaPagamento(FormaPagamento.PIX)
                .valor(new BigDecimal("300.00")).descricao("Dízimo novembro")
                .categoriaId(categoria.getId()).status(StatusDoacao.ATIVA)
                .dataCad(LocalDate.now()).build());

        doacaoRepository.save(Doacao.builder()
                .tipo(TipoDoacao.OFERTA).nomeDoador("Visitante")
                .dataDoacao(LocalDate.of(2026, 11, 12)).formaPagamento(FormaPagamento.DINHEIRO)
                .valor(new BigDecimal("999.00")).descricao("Oferta cancelada")
                .status(StatusDoacao.CANCELADA).dataCad(LocalDate.now()).build());

        contaReceberRepository.save(ContaReceber.builder()
                .descricao("Oferta especial").valor(new BigDecimal("200.00"))
                .vencimento(LocalDate.of(2026, 11, 20)).dataRecebimento(LocalDate.of(2026, 11, 15))
                .frequencia(FrequenciaPagamento.UNICA).status(StatusConta.PAGA)
                .dataCad(LocalDate.now()).build());

        contaComPagamento = contaPagarRepository.save(ContaPagar.builder()
                .descricao("Aluguel").valor(new BigDecimal("1000.00"))
                .vencimento(LocalDate.of(2026, 11, 5)).dataBaixa(LocalDate.of(2026, 11, 5))
                .frequencia(FrequenciaPagamento.MENSAL).status(StatusConta.PAGA)
                .dataCad(LocalDate.now()).build());

        pagamentoDespesaRepository.save(PagamentoDespesa.builder()
                .contaPagarId(contaComPagamento.getId()).valorPago(new BigDecimal("400.00"))
                .dataPagamento(LocalDate.of(2026, 11, 5)).formaPagamento(FormaPagamento.PIX)
                .dataCad(LocalDate.now()).build());

        contaBaixaDireta = contaPagarRepository.save(ContaPagar.builder()
                .descricao("Internet").valor(new BigDecimal("150.00"))
                .vencimento(LocalDate.of(2026, 11, 8)).dataBaixa(LocalDate.of(2026, 11, 8))
                .frequencia(FrequenciaPagamento.MENSAL).status(StatusConta.PAGA)
                .dataCad(LocalDate.now()).build());

        contaPendente = contaPagarRepository.save(ContaPagar.builder()
                .descricao("Água").valor(new BigDecimal("300.00"))
                .vencimento(LocalDate.now().minusDays(2))
                .frequencia(FrequenciaPagamento.MENSAL).status(StatusConta.PENDENTE)
                .dataCad(LocalDate.now()).build());

        pagamentoDespesaRepository.save(PagamentoDespesa.builder()
                .contaPagarId(contaPendente.getId()).valorPago(new BigDecimal("100.00"))
                .dataPagamento(LocalDate.now().minusDays(1)).formaPagamento(FormaPagamento.DINHEIRO)
                .dataCad(LocalDate.now()).build());
    }

    @Test
    @DisplayName("Soma apenas doações ativas do período")
    void somaDoacoesAtivas() {
        assertEquals(new BigDecimal("300.00"),
                doacaoRepository.somarAtivasNoPeriodo(inicio, fim));
    }

    @Test
    @DisplayName("Doação cancelada não entra na soma acumulada")
    void doacaoCanceladaForaDaSoma() {
        assertEquals(new BigDecimal("300.00"),
                doacaoRepository.somarAtivasAte(LocalDate.of(2026, 12, 31)));
    }

    @Test
    @DisplayName("Soma contas a receber quitadas no período")
    void somaContasRecebidas() {
        assertEquals(new BigDecimal("200.00"),
                contaReceberRepository.somarRecebidasNoPeriodo(inicio, fim));
    }

    @Test
    @DisplayName("Soma pagamentos registrados no período, incluindo parciais")
    void somaPagamentos() {
        assertEquals(new BigDecimal("500.00"),
                pagamentoDespesaRepository.somarPagosNoPeriodo(inicio, fim));
    }

    @Test
    @DisplayName("Baixa direta é somada separadamente dos pagamentos")
    void baixaDiretaSomadaSeparadamente() {
        assertEquals(new BigDecimal("150.00"),
                contaPagarRepository.somarBaixadasSemPagamentoNoPeriodo(inicio, fim));
    }

    @Test
    @DisplayName("Baixa direta não é contada duas vezes no acumulado")
    void baixaDiretaNaoDuplicada() {
        BigDecimal pagos = pagamentoDespesaRepository.somarPagosAte(LocalDate.of(2026, 12, 31));
        BigDecimal baixas = contaPagarRepository.somarBaixadasSemPagamentoAte(LocalDate.of(2026, 12, 31));

        assertEquals(new BigDecimal("500.00"), pagos);
        assertEquals(new BigDecimal("150.00"), baixas);
    }

    @Test
    @DisplayName("Valor em aberto considera apenas contas não quitadas")
    void valorEmAberto() {
        assertEquals(new BigDecimal("300.00"), contaPagarRepository.somarValorEmAberto());
    }

    @Test
    @DisplayName("Total pago em contas abertas alimenta o saldo pendente")
    void pagoEmContasAbertas() {
        assertEquals(new BigDecimal("100.00"),
                pagamentoDespesaRepository.somarPagosEmContasAbertas());
    }

    @Test
    @DisplayName("Lista baixas diretas do período")
    void listaBaixasDiretas() {
        List<ContaPagar> lista = contaPagarRepository.listarBaixadasSemPagamentoNoPeriodo(inicio, fim);

        assertEquals(1, lista.size());
        assertEquals("Internet", lista.get(0).getDescricao());
    }

    @Test
    @DisplayName("Contas vencidas em aberto são listadas por vencimento")
    void contasVencidas() {
        List<ContaPagar> lista = contaPagarRepository
                .findByStatusInAndVencimentoBeforeOrderByVencimentoAsc(
                        List.of(StatusConta.PENDENTE, StatusConta.VENCIDA),
                        LocalDate.now());

        assertEquals(1, lista.size());
        assertEquals("Água", lista.get(0).getDescricao());
    }

    @Test
    @DisplayName("Filtro combinado de doações por forma de pagamento")
    void filtroDoacoesPorForma() {
        List<Doacao> lista = doacaoRepository.filtrar(null, inicio, fim, FormaPagamento.PIX, null);

        assertEquals(1, lista.size());
        assertEquals("Dízimo novembro", lista.get(0).getDescricao());
    }

    @Test
    @DisplayName("Filtro de doações por categoria")
    void filtroDoacoesPorCategoria() {
        Long categoriaId = doacaoRepository.findAll().get(0).getCategoriaId();

        List<Doacao> lista = doacaoRepository.filtrar(null, null, null, null, categoriaId);

        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Histórico de saldo inicial retorna o registro mais recente")
    void saldoInicialVigente() {
        saldoInicialRepository.save(SaldoInicial.builder()
                .valor(new BigDecimal("1000.00")).dataReferencia(LocalDate.of(2026, 1, 1))
                .dataCad(LocalDate.now()).build());
        saldoInicialRepository.save(SaldoInicial.builder()
                .valor(new BigDecimal("5000.00")).dataReferencia(LocalDate.of(2026, 6, 1))
                .dataCad(LocalDate.now()).build());

        assertEquals(new BigDecimal("5000.00"),
                saldoInicialRepository.findTopByOrderByDataReferenciaDescIdDesc()
                        .orElseThrow().getValor());
        assertEquals(2, saldoInicialRepository.findAllByOrderByDataReferenciaDescIdDesc().size());
    }
}
