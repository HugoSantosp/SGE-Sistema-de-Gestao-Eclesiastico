package com.sg.financeiro;

import com.sg.financeiro.dto.RelatorioFinanceiroDTO;
import com.sg.fornecedores.FornecedorRepository;
import com.sg.igrejas.Igreja;
import com.sg.igrejas.IgrejaRepository;
import com.sg.shared.enums.FormaPagamento;
import com.sg.shared.enums.FrequenciaPagamento;
import com.sg.shared.enums.StatusConta;
import com.sg.shared.enums.StatusDoacao;
import com.sg.shared.enums.TipoDoacao;
import com.sg.shared.exceptions.BusinessException;
import com.sg.usuario.UsuarioRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelatorioFinanceiroService - Relatórios")
class RelatorioFinanceiroServiceTest {

    @Mock private DoacaoRepository doacaoRepository;
    @Mock private ContaReceberRepository contaReceberRepository;
    @Mock private ContaPagarRepository contaPagarRepository;
    @Mock private PagamentoDespesaRepository pagamentoDespesaRepository;
    @Mock private CategoriaFinanceiraRepository categoriaRepository;
    @Mock private SaldoInicialService saldoInicialService;
    @Mock private IgrejaRepository igrejaRepository;
    @Mock private FornecedorRepository fornecedorRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private RelatorioFinanceiroService relatorioService;

    private final LocalDate inicio = LocalDate.of(2026, 11, 1);
    private final LocalDate fim = LocalDate.of(2026, 11, 30);

    @BeforeEach
    void setUp() {
        lenient().when(categoriaRepository.findAll()).thenReturn(List.of());
        lenient().when(fornecedorRepository.findAll()).thenReturn(List.of());
        lenient().when(igrejaRepository.findAll()).thenReturn(List.of());
        lenient().when(saldoInicialService.saldoVigente()).thenReturn(BigDecimal.ZERO);
        lenient().when(doacaoRepository.findByStatusAndDataDoacaoBetweenOrderByDataDoacaoAscIdAsc(any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(contaReceberRepository.findByStatusAndDataRecebimentoBetween(any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(contaPagarRepository.findAll()).thenReturn(List.of());
        lenient().when(pagamentoDespesaRepository.findByDataPagamentoBetweenOrderByDataPagamentoAscIdAsc(any(), any()))
                .thenReturn(List.of());
        lenient().when(contaPagarRepository.listarBaixadasSemPagamentoNoPeriodo(any(), any()))
                .thenReturn(List.of());
        lenient().when(doacaoRepository.somarAtivasNoPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        lenient().when(contaReceberRepository.somarRecebidasNoPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        lenient().when(pagamentoDespesaRepository.somarPagosNoPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        lenient().when(contaPagarRepository.somarBaixadasSemPagamentoNoPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        lenient().when(doacaoRepository.somarAtivasAte(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(contaReceberRepository.somarRecebidasAte(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(pagamentoDespesaRepository.somarPagosAte(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(contaPagarRepository.somarBaixadasSemPagamentoAte(any())).thenReturn(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Receitas somam doações ativas e contas recebidas")
    void receitasSomamOrigens() {
        when(doacaoRepository.findByStatusAndDataDoacaoBetweenOrderByDataDoacaoAscIdAsc(
                StatusDoacao.ATIVA, inicio, fim))
                .thenReturn(List.of(Doacao.builder().id(1L).valor(new BigDecimal("300.00"))
                        .dataDoacao(LocalDate.of(2026, 11, 10)).descricao("Dízimo")
                        .formaPagamento(FormaPagamento.PIX).build()));
        when(contaReceberRepository.findByStatusAndDataRecebimentoBetween(StatusConta.PAGA, inicio, fim))
                .thenReturn(List.of(ContaReceber.builder().id(2L).valor(new BigDecimal("200.00"))
                        .dataRecebimento(LocalDate.of(2026, 11, 15)).descricao("Oferta").build()));

        RelatorioFinanceiroDTO.RelatorioReceitas relatorio = relatorioService.receitas(inicio, fim);

        assertEquals(2, relatorio.itens().size());
        assertEquals(new BigDecimal("500.00"), relatorio.total());
    }

    @Test
    @DisplayName("Receitas respeitam ordem cronológica")
    void receitasOrdenadasPorData() {
        when(doacaoRepository.findByStatusAndDataDoacaoBetweenOrderByDataDoacaoAscIdAsc(
                StatusDoacao.ATIVA, inicio, fim))
                .thenReturn(List.of(
                        Doacao.builder().id(1L).valor(new BigDecimal("100.00"))
                                .dataDoacao(LocalDate.of(2026, 11, 20)).descricao("Depois").build(),
                        Doacao.builder().id(2L).valor(new BigDecimal("100.00"))
                                .dataDoacao(LocalDate.of(2026, 11, 5)).descricao("Antes").build()));

        RelatorioFinanceiroDTO.RelatorioReceitas relatorio = relatorioService.receitas(inicio, fim);

        assertEquals("Antes", relatorio.itens().get(0).descricao());
        assertEquals("Depois", relatorio.itens().get(1).descricao());
    }

    @Test
    @DisplayName("Despesas trazem valor pago e saldo pendente por conta")
    void despesasComSaldoPendente() {
        ContaPagar conta = ContaPagar.builder().id(5L).descricao("Aluguel")
                .valor(new BigDecimal("1000.00")).vencimento(LocalDate.of(2026, 11, 5))
                .frequencia(FrequenciaPagamento.MENSAL).status(StatusConta.PENDENTE).build();
        when(contaPagarRepository.findAll()).thenReturn(List.of(conta));
        when(pagamentoDespesaRepository.totalPagoPorConta(5L)).thenReturn(new BigDecimal("400.00"));

        RelatorioFinanceiroDTO.RelatorioDespesas relatorio = relatorioService.despesas(inicio, fim);

        assertEquals(1, relatorio.itens().size());
        assertEquals(new BigDecimal("400.00"), relatorio.itens().get(0).valorPago());
        assertEquals(new BigDecimal("600.00"), relatorio.itens().get(0).saldoPendente());
        assertEquals("PARCIALMENTE_PAGA", relatorio.itens().get(0).status());
        assertEquals(new BigDecimal("1000.00"), relatorio.totalOriginal());
        assertEquals(new BigDecimal("600.00"), relatorio.totalPendente());
    }

    @Test
    @DisplayName("Despesas fora do período são ignoradas")
    void despesasForaDoPeriodoIgnoradas() {
        ContaPagar fora = ContaPagar.builder().id(6L).descricao("Antiga")
                .valor(new BigDecimal("500.00")).vencimento(LocalDate.of(2026, 8, 1))
                .frequencia(FrequenciaPagamento.UNICA).status(StatusConta.PAGA).build();
        when(contaPagarRepository.findAll()).thenReturn(List.of(fora));

        RelatorioFinanceiroDTO.RelatorioDespesas relatorio = relatorioService.despesas(inicio, fim);

        assertTrue(relatorio.itens().isEmpty());
    }

    @Test
    @DisplayName("Fluxo de caixa acumula saldo a partir do saldo inicial")
    void fluxoCaixaAcumulaSaldo() {
        when(saldoInicialService.saldoVigente()).thenReturn(new BigDecimal("1000.00"));
        when(doacaoRepository.findByStatusAndDataDoacaoBetweenOrderByDataDoacaoAscIdAsc(
                StatusDoacao.ATIVA, inicio, fim))
                .thenReturn(List.of(Doacao.builder().id(1L).valor(new BigDecimal("500.00"))
                        .dataDoacao(LocalDate.of(2026, 11, 10)).descricao("Dízimo").build()));
        when(pagamentoDespesaRepository.findByDataPagamentoBetweenOrderByDataPagamentoAscIdAsc(inicio, fim))
                .thenReturn(List.of(PagamentoDespesa.builder().id(1L).contaPagarId(3L)
                        .valorPago(new BigDecimal("200.00"))
                        .dataPagamento(LocalDate.of(2026, 11, 12)).build()));

        RelatorioFinanceiroDTO.RelatorioFluxoCaixa relatorio = relatorioService.fluxoCaixa(inicio, fim);

        assertEquals(new BigDecimal("1000.00"), relatorio.saldoInicial());
        assertEquals(2, relatorio.movimentos().size());
        assertEquals(new BigDecimal("1500.00"), relatorio.movimentos().get(0).saldoAcumulado());
        assertEquals(new BigDecimal("1300.00"), relatorio.movimentos().get(1).saldoAcumulado());
        assertEquals(new BigDecimal("1300.00"), relatorio.saldoFinal());
        assertEquals(new BigDecimal("500.00"), relatorio.receitasRecebidas());
        assertEquals(new BigDecimal("200.00"), relatorio.despesasPagas());
    }

    @Test
    @DisplayName("Fluxo de caixa ordena movimentações por data")
    void fluxoCaixaOrdemCronologica() {
        when(doacaoRepository.findByStatusAndDataDoacaoBetweenOrderByDataDoacaoAscIdAsc(
                StatusDoacao.ATIVA, inicio, fim))
                .thenReturn(List.of(Doacao.builder().id(1L).valor(new BigDecimal("100.00"))
                        .dataDoacao(LocalDate.of(2026, 11, 25)).descricao("Tardia").build()));
        when(pagamentoDespesaRepository.findByDataPagamentoBetweenOrderByDataPagamentoAscIdAsc(inicio, fim))
                .thenReturn(List.of(PagamentoDespesa.builder().id(1L).contaPagarId(3L)
                        .valorPago(new BigDecimal("50.00"))
                        .dataPagamento(LocalDate.of(2026, 11, 3)).build()));

        RelatorioFinanceiroDTO.RelatorioFluxoCaixa relatorio = relatorioService.fluxoCaixa(inicio, fim);

        assertEquals(LocalDate.of(2026, 11, 3), relatorio.movimentos().get(0).data());
        assertEquals(LocalDate.of(2026, 11, 25), relatorio.movimentos().get(1).data());
    }

    @Test
    @DisplayName("Demonstrativo calcula resultado e saldo final")
    void demonstrativoCalcula() {
        when(saldoInicialService.saldoVigente()).thenReturn(new BigDecimal("2000.00"));
        when(doacaoRepository.somarAtivasNoPeriodo(inicio, fim)).thenReturn(new BigDecimal("800.00"));
        when(pagamentoDespesaRepository.somarPagosNoPeriodo(inicio, fim)).thenReturn(new BigDecimal("300.00"));
        when(doacaoRepository.somarAtivasAte(fim)).thenReturn(new BigDecimal("800.00"));
        when(pagamentoDespesaRepository.somarPagosAte(fim)).thenReturn(new BigDecimal("300.00"));

        RelatorioFinanceiroDTO.RelatorioDemonstrativo dto = relatorioService.demonstrativo(inicio, fim);

        assertEquals(new BigDecimal("800.00"), dto.totalReceitas());
        assertEquals(new BigDecimal("300.00"), dto.totalDespesasPagas());
        assertEquals(new BigDecimal("500.00"), dto.resultado());
        assertEquals(new BigDecimal("2000.00"), dto.saldoInicial());
        assertEquals(new BigDecimal("2500.00"), dto.saldoFinal());
    }

    @Test
    @DisplayName("Cabeçalho traz nome da igreja e período")
    void cabecalhoComIgreja() {
        when(igrejaRepository.findAll()).thenReturn(List.of(
                Igreja.builder().id(1L).nome("Igreja Central").build()));

        RelatorioFinanceiroDTO.RelatorioDemonstrativo dto = relatorioService.demonstrativo(inicio, fim);

        assertEquals("Igreja Central", dto.cabecalho().igreja());
        assertEquals("Demonstrativo Financeiro", dto.cabecalho().titulo());
        assertEquals(inicio, dto.cabecalho().inicio());
        assertEquals(fim, dto.cabecalho().fim());
    }

    @Test
    @DisplayName("Período invertido é recusado em todos os relatórios")
    void periodoInvertidoRecusado() {
        assertThrows(BusinessException.class, () -> relatorioService.receitas(fim, inicio));
        assertThrows(BusinessException.class, () -> relatorioService.despesas(fim, inicio));
        assertThrows(BusinessException.class, () -> relatorioService.fluxoCaixa(fim, inicio));
        assertThrows(BusinessException.class, () -> relatorioService.demonstrativo(fim, inicio));
    }

    @Test
    @DisplayName("Período nulo é recusado")
    void periodoNuloRecusado() {
        assertThrows(BusinessException.class, () -> relatorioService.receitas(null, fim));
    }
}
