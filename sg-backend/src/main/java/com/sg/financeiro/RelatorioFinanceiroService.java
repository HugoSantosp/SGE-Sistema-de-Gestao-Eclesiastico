package com.sg.financeiro;

import com.sg.financeiro.dto.RelatorioFinanceiroDTO;
import com.sg.fornecedores.Fornecedor;
import com.sg.fornecedores.FornecedorRepository;
import com.sg.igrejas.Igreja;
import com.sg.igrejas.IgrejaRepository;
import com.sg.shared.enums.StatusConta;
import com.sg.shared.enums.StatusDoacao;
import com.sg.shared.exceptions.BusinessException;
import com.sg.shared.security.UsuarioAutenticado;
import com.sg.usuario.Usuario;
import com.sg.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RelatorioFinanceiroService {

    private final DoacaoRepository doacaoRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final ContaPagarRepository contaPagarRepository;
    private final PagamentoDespesaRepository pagamentoDespesaRepository;
    private final CategoriaFinanceiraRepository categoriaRepository;
    private final SaldoInicialService saldoInicialService;
    private final IgrejaRepository igrejaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final UsuarioRepository usuarioRepository;

    public RelatorioFinanceiroService(DoacaoRepository doacaoRepository,
                                      ContaReceberRepository contaReceberRepository,
                                      ContaPagarRepository contaPagarRepository,
                                      PagamentoDespesaRepository pagamentoDespesaRepository,
                                      CategoriaFinanceiraRepository categoriaRepository,
                                      SaldoInicialService saldoInicialService,
                                      IgrejaRepository igrejaRepository,
                                      FornecedorRepository fornecedorRepository,
                                      UsuarioRepository usuarioRepository) {
        this.doacaoRepository = doacaoRepository;
        this.contaReceberRepository = contaReceberRepository;
        this.contaPagarRepository = contaPagarRepository;
        this.pagamentoDespesaRepository = pagamentoDespesaRepository;
        this.categoriaRepository = categoriaRepository;
        this.saldoInicialService = saldoInicialService;
        this.igrejaRepository = igrejaRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ==================== RECEITAS ====================
    public RelatorioFinanceiroDTO.RelatorioReceitas receitas(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        Map<Long, String> categorias = nomesCategorias();

        List<RelatorioFinanceiroDTO.ReceitaItem> itens = new ArrayList<>();

        doacaoRepository.findByStatusAndDataDoacaoBetweenOrderByDataDoacaoAscIdAsc(
                        StatusDoacao.ATIVA, inicio, fim)
                .forEach(d -> itens.add(new RelatorioFinanceiroDTO.ReceitaItem(
                        d.getDataDoacao(), d.getDescricao(),
                        nomeCategoria(d.getCategoriaId(), categorias),
                        d.getFormaPagamento() != null ? d.getFormaPagamento().name() : "-",
                        d.getValor())));

        contaReceberRepository.findByStatusAndDataRecebimentoBetween(StatusConta.PAGA, inicio, fim)
                .forEach(c -> itens.add(new RelatorioFinanceiroDTO.ReceitaItem(
                        c.getDataRecebimento(), c.getDescricao(),
                        nomeCategoria(c.getCategoriaId(), categorias),
                        c.getFormaPagamento() != null ? c.getFormaPagamento().name() : "-",
                        c.getValor())));

        itens.sort(Comparator.comparing(RelatorioFinanceiroDTO.ReceitaItem::data));

        BigDecimal total = itens.stream()
                .map(RelatorioFinanceiroDTO.ReceitaItem::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RelatorioFinanceiroDTO.RelatorioReceitas(
                cabecalho("Relatório de Receitas", inicio, fim), itens, total);
    }

    // ==================== DESPESAS ====================
    public RelatorioFinanceiroDTO.RelatorioDespesas despesas(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        Map<Long, String> categorias = nomesCategorias();
        Map<Long, String> fornecedores = nomesFornecedores();

        List<RelatorioFinanceiroDTO.DespesaItem> itens = new ArrayList<>();

        for (ContaPagar conta : contaPagarRepository.findAll()) {
            LocalDate referencia = conta.getVencimento() != null ? conta.getVencimento() : conta.getDataCad();
            if (referencia == null || referencia.isBefore(inicio) || referencia.isAfter(fim)) {
                continue;
            }
            BigDecimal pago = pagamentoDespesaRepository.totalPagoPorConta(conta.getId());
            BigDecimal pagoSeguro = pago == null ? BigDecimal.ZERO : pago;
            itens.add(new RelatorioFinanceiroDTO.DespesaItem(
                    conta.getDescricao(),
                    nomeCategoria(conta.getCategoriaId(), categorias),
                    conta.getFornecedorId() == null ? "-"
                            : fornecedores.getOrDefault(conta.getFornecedorId(), "-"),
                    conta.getVencimento(),
                    conta.getValor(),
                    pagoSeguro,
                    conta.getValor().subtract(pagoSeguro),
                    conta.statusExibicao(pagoSeguro)));
        }

        itens.sort(Comparator.comparing(RelatorioFinanceiroDTO.DespesaItem::vencimento,
                Comparator.nullsLast(Comparator.naturalOrder())));

        BigDecimal totalOriginal = itens.stream()
                .map(RelatorioFinanceiroDTO.DespesaItem::valorOriginal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPago = itens.stream()
                .map(RelatorioFinanceiroDTO.DespesaItem::valorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPendente = itens.stream()
                .map(RelatorioFinanceiroDTO.DespesaItem::saldoPendente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RelatorioFinanceiroDTO.RelatorioDespesas(
                cabecalho("Relatório de Despesas", inicio, fim),
                itens, totalOriginal, totalPago, totalPendente);
    }

    // ==================== FLUXO DE CAIXA ====================
    public RelatorioFinanceiroDTO.RelatorioFluxoCaixa fluxoCaixa(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);

        BigDecimal saldoInicial = saldoInicialService.saldoVigente();

        List<RelatorioFinanceiroDTO.MovimentoCaixa> brutos = new ArrayList<>();

        doacaoRepository.findByStatusAndDataDoacaoBetweenOrderByDataDoacaoAscIdAsc(
                        StatusDoacao.ATIVA, inicio, fim)
                .forEach(d -> brutos.add(new RelatorioFinanceiroDTO.MovimentoCaixa(
                        d.getDataDoacao(), d.getDescricao(), d.getValor(), BigDecimal.ZERO, null)));

        contaReceberRepository.findByStatusAndDataRecebimentoBetween(StatusConta.PAGA, inicio, fim)
                .forEach(c -> brutos.add(new RelatorioFinanceiroDTO.MovimentoCaixa(
                        c.getDataRecebimento(), c.getDescricao(), c.getValor(), BigDecimal.ZERO, null)));

        pagamentoDespesaRepository.findByDataPagamentoBetweenOrderByDataPagamentoAscIdAsc(inicio, fim)
                .forEach(p -> brutos.add(new RelatorioFinanceiroDTO.MovimentoCaixa(
                        p.getDataPagamento(), "Pagamento da conta #" + p.getContaPagarId(),
                        BigDecimal.ZERO, p.getValorPago(), null)));

        contaPagarRepository.listarBaixadasSemPagamentoNoPeriodo(inicio, fim)
                .forEach(c -> brutos.add(new RelatorioFinanceiroDTO.MovimentoCaixa(
                        c.getDataBaixa(), c.getDescricao(), BigDecimal.ZERO, c.getValor(), null)));

        // Ordem cronológica obrigatória; saldo acumulado recalculado a cada linha.
        brutos.sort(Comparator.comparing(RelatorioFinanceiroDTO.MovimentoCaixa::data));

        List<RelatorioFinanceiroDTO.MovimentoCaixa> movimentos = new ArrayList<>();
        BigDecimal acumulado = saldoInicial;
        for (RelatorioFinanceiroDTO.MovimentoCaixa m : brutos) {
            acumulado = acumulado.add(m.entradas()).subtract(m.saidas());
            movimentos.add(new RelatorioFinanceiroDTO.MovimentoCaixa(
                    m.data(), m.descricao(), m.entradas(), m.saidas(), acumulado));
        }

        BigDecimal receitas = movimentos.stream()
                .map(RelatorioFinanceiroDTO.MovimentoCaixa::entradas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal despesas = movimentos.stream()
                .map(RelatorioFinanceiroDTO.MovimentoCaixa::saidas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RelatorioFinanceiroDTO.RelatorioFluxoCaixa(
                cabecalho("Fluxo de Caixa", inicio, fim),
                saldoInicial, receitas, despesas, movimentos, acumulado);
    }

    // ==================== DEMONSTRATIVO ====================
    public RelatorioFinanceiroDTO.RelatorioDemonstrativo demonstrativo(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);

        BigDecimal receitas = doacaoRepository.somarAtivasNoPeriodo(inicio, fim)
                .add(contaReceberRepository.somarRecebidasNoPeriodo(inicio, fim));
        BigDecimal despesas = pagamentoDespesaRepository.somarPagosNoPeriodo(inicio, fim)
                .add(contaPagarRepository.somarBaixadasSemPagamentoNoPeriodo(inicio, fim));

        BigDecimal saldoInicial = saldoInicialService.saldoVigente();
        BigDecimal saldoFinal = saldoInicial
                .add(doacaoRepository.somarAtivasAte(fim))
                .add(contaReceberRepository.somarRecebidasAte(fim))
                .subtract(pagamentoDespesaRepository.somarPagosAte(fim))
                .subtract(contaPagarRepository.somarBaixadasSemPagamentoAte(fim));

        return new RelatorioFinanceiroDTO.RelatorioDemonstrativo(
                cabecalho("Demonstrativo Financeiro", inicio, fim),
                receitas, despesas, receitas.subtract(despesas), saldoInicial, saldoFinal);
    }

    // ==================== APOIO ====================
    private RelatorioFinanceiroDTO cabecalho(String titulo, LocalDate inicio, LocalDate fim) {
        return new RelatorioFinanceiroDTO(
                titulo, nomeIgreja(), inicio, fim, nomeResponsavel(), LocalDate.now());
    }

    private String nomeIgreja() {
        return igrejaRepository.findAll().stream()
                .findFirst()
                .map(Igreja::getNome)
                .orElse("SGE");
    }

    private String nomeResponsavel() {
        Long id = UsuarioAutenticado.id();
        if (id == null) {
            return "-";
        }
        return usuarioRepository.findById(id)
                .map(Usuario::getNome)
                .orElse("-");
    }

    private Map<Long, String> nomesCategorias() {
        return categoriaRepository.findAll().stream()
                .collect(Collectors.toMap(CategoriaFinanceira::getId, CategoriaFinanceira::getNome));
    }

    private Map<Long, String> nomesFornecedores() {
        return fornecedorRepository.findAll().stream()
                .collect(Collectors.toMap(Fornecedor::getId, Fornecedor::getNome));
    }

    private String nomeCategoria(Long id, Map<Long, String> categorias) {
        if (id == null) {
            return "Sem categoria";
        }
        return categorias.getOrDefault(id, "Sem categoria");
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new BusinessException("Informe o período inicial e final do relatório.");
        }
        if (inicio.isAfter(fim)) {
            throw new BusinessException("A data inicial não pode ser posterior à data final.");
        }
    }
}
