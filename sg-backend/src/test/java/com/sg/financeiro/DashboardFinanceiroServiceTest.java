package com.sg.financeiro;

import com.sg.financeiro.dto.DashboardResponseDTO;
import com.sg.shared.enums.StatusConta;
import com.sg.shared.enums.StatusDoacao;
import com.sg.shared.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardFinanceiroService - Indicadores consolidados")
class DashboardFinanceiroServiceTest {

    @Mock private ContaPagarRepository contaPagarRepository;
    @Mock private ContaReceberRepository contaReceberRepository;
    @Mock private PagamentoDespesaRepository pagamentoDespesaRepository;
    @Mock private DoacaoRepository doacaoRepository;
    @Mock private CategoriaFinanceiraRepository categoriaRepository;
    @Mock private SaldoInicialService saldoInicialService;

    @InjectMocks private DashboardFinanceiroService dashboardService;

    private final LocalDate inicio = LocalDate.of(2026, 11, 1);
    private final LocalDate fim = LocalDate.of(2026, 11, 30);

    @BeforeEach
    void setUp() {
        lenient().when(doacaoRepository.somarAtivasNoPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        lenient().when(contaReceberRepository.somarRecebidasNoPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        lenient().when(pagamentoDespesaRepository.somarPagosNoPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        lenient().when(contaPagarRepository.somarBaixadasSemPagamentoNoPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        lenient().when(doacaoRepository.somarAtivasAte(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(contaReceberRepository.somarRecebidasAte(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(pagamentoDespesaRepository.somarPagosAte(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(contaPagarRepository.somarBaixadasSemPagamentoAte(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(contaPagarRepository.somarValorEmAberto()).thenReturn(BigDecimal.ZERO);
        lenient().when(pagamentoDespesaRepository.somarPagosEmContasAbertas()).thenReturn(BigDecimal.ZERO);
        lenient().when(saldoInicialService.saldoVigente()).thenReturn(BigDecimal.ZERO);
        lenient().when(categoriaRepository.findAll()).thenReturn(List.of());
        lenient().when(doacaoRepository.findByStatusAndDataDoacaoBetweenOrderByDataDoacaoAscIdAsc(any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(contaReceberRepository.findByStatusAndDataRecebimentoBetween(any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(pagamentoDespesaRepository.findByDataPagamentoBetweenOrderByDataPagamentoAscIdAsc(any(), any()))
                .thenReturn(List.of());
        lenient().when(contaPagarRepository.listarBaixadasSemPagamentoNoPeriodo(any(), any()))
                .thenReturn(List.of());
        lenient().when(contaPagarRepository.findByStatusAndDataBaixaBetween(any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(contaPagarRepository.findByStatusInAndVencimentoBetweenOrderByVencimentoAsc(any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(contaPagarRepository.findByStatusInAndVencimentoBeforeOrderByVencimentoAsc(any(), any()))
                .thenReturn(List.of());
    }

    @Test
    @DisplayName("Resultado é receitas recebidas menos despesas pagas")
    void resultadoConsideraApenasEfetivados() {
        when(doacaoRepository.somarAtivasNoPeriodo(inicio, fim)).thenReturn(new BigDecimal("1000.00"));
        when(pagamentoDespesaRepository.somarPagosNoPeriodo(inicio, fim)).thenReturn(new BigDecimal("400.00"));

        DashboardResponseDTO dto = dashboardService.calcular(inicio, fim);

        assertEquals(new BigDecimal("1000.00"), dto.indicadores().receitasRecebidas());
        assertEquals(new BigDecimal("400.00"), dto.indicadores().despesasPagas());
        assertEquals(new BigDecimal("600.00"), dto.indicadores().resultado());
    }

    @Test
    @DisplayName("Despesa pendente não entra como despesa paga")
    void despesaPendenteNaoEntraComoPaga() {
        when(contaPagarRepository.somarValorEmAberto()).thenReturn(new BigDecimal("700.00"));
        when(pagamentoDespesaRepository.somarPagosEmContasAbertas()).thenReturn(new BigDecimal("200.00"));

        DashboardResponseDTO dto = dashboardService.calcular(inicio, fim);

        assertEquals(BigDecimal.ZERO, dto.indicadores().despesasPagas());
        assertEquals(new BigDecimal("500.00"), dto.indicadores().contasPendentes());
    }

    @Test
    @DisplayName("Saldo disponível soma saldo inicial e movimentações efetivadas até o fim")
    void saldoDisponivelSomaSaldoInicial() {
        when(saldoInicialService.saldoVigente()).thenReturn(new BigDecimal("5000.00"));
        when(doacaoRepository.somarAtivasAte(fim)).thenReturn(new BigDecimal("3000.00"));
        when(pagamentoDespesaRepository.somarPagosAte(fim)).thenReturn(new BigDecimal("1200.00"));

        DashboardResponseDTO dto = dashboardService.calcular(inicio, fim);

        assertEquals(new BigDecimal("5000.00"), dto.indicadores().saldoInicial());
        assertEquals(new BigDecimal("6800.00"), dto.indicadores().saldoDisponivel());
    }

    @Test
    @DisplayName("Período invertido é recusado")
    void periodoInvertidoRecusado() {
        assertThrows(BusinessException.class,
                () -> dashboardService.calcular(fim, inicio));
    }

    @Test
    @DisplayName("Evolução mensal preenche meses sem movimento com zero")
    void evolucaoMensalPreencheMesesVazios() {
        DashboardResponseDTO dto = dashboardService.calcular(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 31));

        assertEquals(3, dto.evolucaoMensal().size());
        assertEquals("2026-10", dto.evolucaoMensal().get(0).mes());
        assertEquals("2026-12", dto.evolucaoMensal().get(2).mes());
        assertEquals(BigDecimal.ZERO, dto.evolucaoMensal().get(1).receitas());
    }

    @Test
    @DisplayName("Distribuição por categoria agrupa receitas do período")
    void distribuicaoPorCategoriaAgrupa() {
        CategoriaFinanceira dizimos = CategoriaFinanceira.builder()
                .id(1L).nome("Dízimos").build();
        when(categoriaRepository.findAll()).thenReturn(List.of(dizimos));
        when(doacaoRepository.findByStatusAndDataDoacaoBetweenOrderByDataDoacaoAscIdAsc(
                StatusDoacao.ATIVA, inicio, fim))
                .thenReturn(List.of(
                        Doacao.builder().id(1L).valor(new BigDecimal("100.00")).categoriaId(1L)
                                .dataDoacao(LocalDate.of(2026, 11, 5)).descricao("Dízimo A").build(),
                        Doacao.builder().id(2L).valor(new BigDecimal("250.00")).categoriaId(1L)
                                .dataDoacao(LocalDate.of(2026, 11, 6)).descricao("Dízimo B").build()));

        DashboardResponseDTO dto = dashboardService.calcular(inicio, fim);

        assertEquals(1, dto.receitasPorCategoria().size());
        assertEquals("Dízimos", dto.receitasPorCategoria().get(0).categoria());
        assertEquals(new BigDecimal("350.00"), dto.receitasPorCategoria().get(0).valor());
    }

    @Test
    @DisplayName("Conta vencida traz saldo em aberto descontando o pago")
    void contaVencidaDescontaPago() {
        ContaPagar conta = ContaPagar.builder().id(9L).descricao("Aluguel")
                .valor(new BigDecimal("1000.00")).vencimento(LocalDate.now().minusDays(3))
                .status(StatusConta.PENDENTE).build();
        when(contaPagarRepository.findByStatusInAndVencimentoBeforeOrderByVencimentoAsc(any(), any()))
                .thenReturn(List.of(conta));
        when(pagamentoDespesaRepository.totalPagoPorConta(9L)).thenReturn(new BigDecimal("400.00"));

        DashboardResponseDTO dto = dashboardService.calcular(inicio, fim);

        assertEquals(1, dto.contasVencidas().size());
        assertEquals(new BigDecimal("600.00"), dto.contasVencidas().get(0).saldoAberto());
        assertEquals("PARCIALMENTE_PAGA", dto.contasVencidas().get(0).status());
    }

    @Test
    @DisplayName("Variação é nula quando o período anterior era zero")
    void variacaoNulaComPeriodoAnteriorZerado() {
        DashboardResponseDTO dto = dashboardService.calcular(inicio, fim);

        assertNull(dto.comparativo().variacaoReceitas());
        assertNull(dto.comparativo().variacaoDespesas());
    }

    @Test
    @DisplayName("Variação percentual compara com o período anterior de mesmo tamanho")
    void variacaoPercentual() {
        when(doacaoRepository.somarAtivasNoPeriodo(eq(inicio), eq(fim)))
                .thenReturn(new BigDecimal("300.00"));
        when(doacaoRepository.somarAtivasNoPeriodo(eq(LocalDate.of(2026, 10, 2)), eq(LocalDate.of(2026, 10, 31))))
                .thenReturn(new BigDecimal("200.00"));

        DashboardResponseDTO dto = dashboardService.calcular(inicio, fim);

        assertEquals(new BigDecimal("50.00"), dto.comparativo().variacaoReceitas());
    }
}
