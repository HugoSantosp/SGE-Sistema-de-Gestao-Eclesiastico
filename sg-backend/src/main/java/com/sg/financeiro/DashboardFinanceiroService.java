package com.sg.financeiro;

import com.sg.financeiro.dto.DashboardResponseDTO;
import com.sg.shared.enums.StatusConta;
import com.sg.shared.enums.StatusDoacao;
import com.sg.shared.exceptions.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Consolida os indicadores financeiros. Todo cálculo acontece aqui: o frontend
 * recebe apenas valores já apurados.
 */
@Service
public class DashboardFinanceiroService {

    private static final int DIAS_VENCIMENTO_PROXIMO = 7;
    private static final int LIMITE_MOVIMENTACOES = 10;
    private static final List<StatusConta> STATUS_EM_ABERTO = List.of(
            StatusConta.PENDENTE, StatusConta.VENCIDA);

    private final ContaPagarRepository contaPagarRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final PagamentoDespesaRepository pagamentoDespesaRepository;
    private final DoacaoRepository doacaoRepository;
    private final CategoriaFinanceiraRepository categoriaRepository;
    private final SaldoInicialService saldoInicialService;

    public DashboardFinanceiroService(ContaPagarRepository contaPagarRepository,
                                      ContaReceberRepository contaReceberRepository,
                                      PagamentoDespesaRepository pagamentoDespesaRepository,
                                      DoacaoRepository doacaoRepository,
                                      CategoriaFinanceiraRepository categoriaRepository,
                                      SaldoInicialService saldoInicialService) {
        this.contaPagarRepository = contaPagarRepository;
        this.contaReceberRepository = contaReceberRepository;
        this.pagamentoDespesaRepository = pagamentoDespesaRepository;
        this.doacaoRepository = doacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.saldoInicialService = saldoInicialService;
    }

    public DashboardResponseDTO calcular(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new BusinessException("Informe o período inicial e final da dashboard.");
        }
        if (inicio.isAfter(fim)) {
            throw new BusinessException("A data inicial não pode ser posterior à data final.");
        }

        BigDecimal receitas = receitasRecebidas(inicio, fim);
        BigDecimal despesas = despesasPagas(inicio, fim);
        BigDecimal resultado = receitas.subtract(despesas);

        BigDecimal saldoInicial = saldoInicialService.saldoVigente();
        BigDecimal saldoDisponivel = saldoInicial
                .add(receitasAcumuladasAte(fim))
                .subtract(despesasAcumuladasAte(fim));

        DashboardResponseDTO.Indicadores indicadores = new DashboardResponseDTO.Indicadores(
                receitas, despesas, resultado, contasPendentes(), saldoInicial, saldoDisponivel);

        List<DashboardResponseDTO.Movimentacao> movimentacoes = movimentacoes(inicio, fim);
        Map<Long, String> nomesCategorias = nomesCategorias();

        return new DashboardResponseDTO(
                inicio,
                fim,
                indicadores,
                comparativo(inicio, fim, receitas, despesas, resultado),
                evolucaoMensal(inicio, fim, movimentacoes),
                distribuicaoPorCategoria(movimentacoes, "RECEITA", nomesCategorias),
                distribuicaoPorCategoria(movimentacoes, "DESPESA", nomesCategorias),
                movimentacoes.stream()
                        .sorted(Comparator.comparing(DashboardResponseDTO.Movimentacao::data).reversed())
                        .limit(LIMITE_MOVIMENTACOES)
                        .toList(),
                contasVencendo(),
                contasVencidas()
        );
    }

    // ------ INDICADORES ------
    private BigDecimal receitasRecebidas(LocalDate inicio, LocalDate fim) {
        return doacaoRepository.somarAtivasNoPeriodo(inicio, fim)
                .add(contaReceberRepository.somarRecebidasNoPeriodo(inicio, fim));
    }

    private BigDecimal despesasPagas(LocalDate inicio, LocalDate fim) {
        return pagamentoDespesaRepository.somarPagosNoPeriodo(inicio, fim)
                .add(contaPagarRepository.somarBaixadasSemPagamentoNoPeriodo(inicio, fim));
    }

    private BigDecimal receitasAcumuladasAte(LocalDate data) {
        return doacaoRepository.somarAtivasAte(data)
                .add(contaReceberRepository.somarRecebidasAte(data));
    }

    private BigDecimal despesasAcumuladasAte(LocalDate data) {
        return pagamentoDespesaRepository.somarPagosAte(data)
                .add(contaPagarRepository.somarBaixadasSemPagamentoAte(data));
    }

    /** Saldo em aberto das contas a pagar: valor original menos o já pago. */
    private BigDecimal contasPendentes() {
        BigDecimal emAberto = contaPagarRepository.somarValorEmAberto()
                .subtract(pagamentoDespesaRepository.somarPagosEmContasAbertas());
        return emAberto.max(BigDecimal.ZERO);
    }

    // ------ COMPARATIVO ------
    private DashboardResponseDTO.Comparativo comparativo(LocalDate inicio, LocalDate fim,
                                                         BigDecimal receitas, BigDecimal despesas,
                                                         BigDecimal resultado) {
        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fim) + 1;
        LocalDate fimAnterior = inicio.minusDays(1);
        LocalDate inicioAnterior = fimAnterior.minusDays(dias - 1);

        BigDecimal receitasAnterior = receitasRecebidas(inicioAnterior, fimAnterior);
        BigDecimal despesasAnterior = despesasPagas(inicioAnterior, fimAnterior);

        return new DashboardResponseDTO.Comparativo(
                inicioAnterior, fimAnterior,
                receitasAnterior, despesasAnterior,
                receitasAnterior.subtract(despesasAnterior),
                variacao(receitas, receitasAnterior),
                variacao(despesas, despesasAnterior));
    }

    private BigDecimal variacao(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return atual.subtract(anterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(anterior, 2, RoundingMode.HALF_UP);
    }

    // ------ MOVIMENTACOES ------
    private List<DashboardResponseDTO.Movimentacao> movimentacoes(LocalDate inicio, LocalDate fim) {
        List<DashboardResponseDTO.Movimentacao> lista = new ArrayList<>();

        doacaoRepository.findByStatusAndDataDoacaoBetweenOrderByDataDoacaoAscIdAsc(
                        StatusDoacao.ATIVA, inicio, fim)
                .forEach(d -> lista.add(new DashboardResponseDTO.Movimentacao(
                        d.getDataDoacao(), d.getDescricao(), "RECEITA", d.getValor(), d.getCategoriaId())));

        contaReceberRepository.findByStatusAndDataRecebimentoBetween(StatusConta.PAGA, inicio, fim)
                .forEach(c -> lista.add(new DashboardResponseDTO.Movimentacao(
                        c.getDataRecebimento(), c.getDescricao(), "RECEITA", c.getValor(), c.getCategoriaId())));

        Map<Long, Long> categoriaPorConta = contaPagarRepository
                .findByStatusAndDataBaixaBetween(StatusConta.PAGA, inicio, fim).stream()
                .filter(c -> c.getCategoriaId() != null)
                .collect(Collectors.toMap(ContaPagar::getId, ContaPagar::getCategoriaId, (a, b) -> a));

        pagamentoDespesaRepository.findByDataPagamentoBetweenOrderByDataPagamentoAscIdAsc(inicio, fim)
                .forEach(p -> lista.add(new DashboardResponseDTO.Movimentacao(
                        p.getDataPagamento(), "Pagamento da conta #" + p.getContaPagarId(),
                        "DESPESA", p.getValorPago(), categoriaPorConta.get(p.getContaPagarId()))));

        contaPagarRepository.listarBaixadasSemPagamentoNoPeriodo(inicio, fim)
                .forEach(c -> lista.add(new DashboardResponseDTO.Movimentacao(
                        c.getDataBaixa(), c.getDescricao(), "DESPESA", c.getValor(), c.getCategoriaId())));

        return lista;
    }

    private List<DashboardResponseDTO.EvolucaoMensal> evolucaoMensal(
            LocalDate inicio, LocalDate fim, List<DashboardResponseDTO.Movimentacao> movimentacoes) {

        Map<YearMonth, BigDecimal[]> porMes = new LinkedHashMap<>();
        YearMonth atual = YearMonth.from(inicio);
        YearMonth ultimo = YearMonth.from(fim);
        while (!atual.isAfter(ultimo)) {
            porMes.put(atual, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            atual = atual.plusMonths(1);
        }

        for (DashboardResponseDTO.Movimentacao m : movimentacoes) {
            BigDecimal[] valores = porMes.get(YearMonth.from(m.data()));
            if (valores == null) continue;
            int indice = "RECEITA".equals(m.tipo()) ? 0 : 1;
            valores[indice] = valores[indice].add(m.valor());
        }

        return porMes.entrySet().stream()
                .map(e -> new DashboardResponseDTO.EvolucaoMensal(
                        e.getKey().toString(), e.getValue()[0], e.getValue()[1]))
                .toList();
    }

    private List<DashboardResponseDTO.DistribuicaoCategoria> distribuicaoPorCategoria(
            List<DashboardResponseDTO.Movimentacao> movimentacoes,
            String tipo,
            Map<Long, String> nomesCategorias) {

        Map<Long, BigDecimal> totais = new LinkedHashMap<>();
        for (DashboardResponseDTO.Movimentacao m : movimentacoes) {
            if (!tipo.equals(m.tipo())) continue;
            Long chave = m.categoriaId();
            totais.merge(chave, m.valor(), BigDecimal::add);
        }

        return totais.entrySet().stream()
                .map(e -> new DashboardResponseDTO.DistribuicaoCategoria(
                        e.getKey(),
                        e.getKey() == null ? "Sem categoria" : nomesCategorias.getOrDefault(e.getKey(), "Sem categoria"),
                        e.getValue()))
                .sorted(Comparator.comparing(DashboardResponseDTO.DistribuicaoCategoria::valor).reversed())
                .toList();
    }

    private Map<Long, String> nomesCategorias() {
        return categoriaRepository.findAll().stream()
                .collect(Collectors.toMap(CategoriaFinanceira::getId, CategoriaFinanceira::getNome));
    }

    // ------ CONTAS ------
    private List<DashboardResponseDTO.ContaResumo> contasVencendo() {
        LocalDate hoje = LocalDate.now();
        return contaPagarRepository
                .findByStatusInAndVencimentoBetweenOrderByVencimentoAsc(
                        STATUS_EM_ABERTO, hoje, hoje.plusDays(DIAS_VENCIMENTO_PROXIMO))
                .stream().map(this::toResumo).toList();
    }

    private List<DashboardResponseDTO.ContaResumo> contasVencidas() {
        return contaPagarRepository
                .findByStatusInAndVencimentoBeforeOrderByVencimentoAsc(STATUS_EM_ABERTO, LocalDate.now())
                .stream().map(this::toResumo).toList();
    }

    private DashboardResponseDTO.ContaResumo toResumo(ContaPagar conta) {
        BigDecimal pago = pagamentoDespesaRepository.totalPagoPorConta(conta.getId());
        BigDecimal pagoSeguro = pago == null ? BigDecimal.ZERO : pago;
        return new DashboardResponseDTO.ContaResumo(
                conta.getId(), conta.getDescricao(), conta.getVencimento(),
                conta.getValor(), pagoSeguro,
                conta.getValor().subtract(pagoSeguro), conta.statusExibicao(pagoSeguro));
    }
}
