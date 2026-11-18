package com.sg.financeiro;

import com.sg.financeiro.dto.PagamentoRequestDTO;
import com.sg.shared.enums.StatusConta;
import com.sg.shared.exceptions.BusinessException;
import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FinanceiroService {

    private final ContaPagarRepository contaPagarRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final PagamentoDespesaRepository pagamentoDespesaRepository;

    public FinanceiroService(ContaPagarRepository contaPagarRepository,
                             ContaReceberRepository contaReceberRepository,
                             PagamentoDespesaRepository pagamentoDespesaRepository) {
        this.contaPagarRepository = contaPagarRepository;
        this.contaReceberRepository = contaReceberRepository;
        this.pagamentoDespesaRepository = pagamentoDespesaRepository;
    }

    // ------ CONTAS A PAGAR ------
    public List<ContaPagar> listarContasPagar() {
        return contaPagarRepository.findAll();
    }

    public ContaPagar buscarContaPagar(Long id) {
        return contaPagarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a pagar", id));
    }

    @Transactional
    public ContaPagar salvarContaPagar(ContaPagar conta) {
        if (conta.getDataCad() == null) conta.setDataCad(LocalDate.now());
        if (conta.getStatus() == null) conta.setStatus(StatusConta.PENDENTE);
        return contaPagarRepository.save(conta);
    }

    @Transactional
    public ContaPagar atualizarContaPagar(Long id, ContaPagar dados) {
        ContaPagar conta = buscarContaPagar(id);
        if (conta.getStatus() == StatusConta.CANCELADA) {
            throw new BusinessException("Conta cancelada não pode ser alterada.");
        }
        if (conta.getStatus() == StatusConta.PAGA) {
            throw new BusinessException("Conta já paga não pode ser alterada. Registre um ajuste.");
        }
        BigDecimal pago = totalPago(id);
        if (dados.getValor() != null && dados.getValor().compareTo(pago) < 0) {
            throw new BusinessException("O valor da conta não pode ser menor que o total já pago (R$ " + pago + ").");
        }

        conta.setDescricao(dados.getDescricao());
        conta.setFornecedorId(dados.getFornecedorId());
        conta.setValor(dados.getValor());
        conta.setVencimento(dados.getVencimento());
        conta.setDataEmissao(dados.getDataEmissao());
        conta.setCategoriaId(dados.getCategoriaId());
        conta.setFrequencia(dados.getFrequencia());
        conta.setObservacao(dados.getObservacao());
        conta.setArquivo(dados.getArquivo());
        return contaPagarRepository.save(conta);
    }

    /**
     * Atalho de baixa integral mantido para compatibilidade com o endpoint existente.
     * O fluxo com histórico de pagamentos é registrarPagamento.
     */
    @Transactional
    public ContaPagar baixarContaPagar(Long id) {
        ContaPagar conta = buscarContaPagar(id);
        if (conta.getStatus() == StatusConta.CANCELADA) {
            throw new BusinessException("Conta cancelada não pode ser baixada.");
        }
        conta.setStatus(StatusConta.PAGA);
        conta.setDataBaixa(LocalDate.now());
        return contaPagarRepository.save(conta);
    }

    /**
     * Alteração manual do status pelo tesoureiro ou pelo bispo.
     * Cancelamento não passa por aqui: exige justificativa no endpoint próprio.
     */
    @Transactional
    public ContaPagar alterarStatusContaPagar(Long id, StatusConta novoStatus) {
        ContaPagar conta = buscarContaPagar(id);
        if (novoStatus == StatusConta.CANCELADA) {
            throw new BusinessException("Para cancelar, use o cancelamento com justificativa.");
        }
        if (conta.getStatus() == StatusConta.CANCELADA) {
            throw new BusinessException("Conta cancelada não pode ter o status alterado.");
        }
        if (conta.getStatus() == novoStatus) {
            return conta;
        }

        BigDecimal pago = totalPago(id);
        if (novoStatus == StatusConta.PAGA) {
            if (pago.compareTo(BigDecimal.ZERO) > 0 && pago.compareTo(conta.getValor()) < 0) {
                throw new BusinessException("A conta tem pagamento parcial de R$ " + pago
                        + ". Registre o pagamento restante para quitá-la.");
            }
            conta.setStatus(StatusConta.PAGA);
            if (conta.getDataBaixa() == null) {
                conta.setDataBaixa(LocalDate.now());
            }
        } else {
            if (pago.compareTo(conta.getValor()) >= 0) {
                throw new BusinessException("Conta já quitada não pode voltar para " + novoStatus + ".");
            }
            conta.setStatus(novoStatus);
            conta.setDataBaixa(null);
        }

        return contaPagarRepository.save(conta);
    }

    @Transactional
    public ContaPagar cancelarContaPagar(Long id, String justificativa, Long usuarioId) {
        ContaPagar conta = buscarContaPagar(id);
        if (conta.getStatus() == StatusConta.CANCELADA) {
            throw new BusinessException("Conta já está cancelada.");
        }
        if (totalPago(id).compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Conta com pagamento registrado não pode ser cancelada.");
        }
        aplicarCancelamento(conta, justificativa, usuarioId);
        return contaPagarRepository.save(conta);
    }

    @Transactional
    public void deletarContaPagar(Long id) {
        ContaPagar conta = contaPagarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a pagar", id));
        if (conta.getStatus() != StatusConta.PENDENTE) {
            throw new BusinessException(
                    "Somente conta pendente pode ser excluída. Use o cancelamento para preservar o histórico.");
        }
        if (totalPago(id).compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Conta com pagamento registrado não pode ser excluída.");
        }
        contaPagarRepository.delete(conta);
    }

    // ------ PAGAMENTOS DE DESPESA ------
    @Transactional
    public ContaPagar registrarPagamento(Long contaPagarId, PagamentoRequestDTO dto, Long usuarioId) {
        ContaPagar conta = buscarContaPagar(contaPagarId);
        if (conta.getStatus() == StatusConta.CANCELADA) {
            throw new BusinessException("Conta cancelada não aceita pagamento.");
        }
        if (conta.getStatus() == StatusConta.PAGA) {
            throw new BusinessException("Conta já está integralmente paga.");
        }

        BigDecimal jaPago = totalPago(contaPagarId);
        BigDecimal novoTotal = jaPago.add(dto.valorPago());
        if (novoTotal.compareTo(conta.getValor()) > 0) {
            BigDecimal saldo = conta.getValor().subtract(jaPago);
            throw new BusinessException("Valor excede o saldo em aberto de R$ " + saldo + ".");
        }

        PagamentoDespesa pagamento = PagamentoDespesa.builder()
                .contaPagarId(contaPagarId)
                .valorPago(dto.valorPago())
                .dataPagamento(dto.dataPagamento())
                .formaPagamento(dto.formaPagamento())
                .observacao(dto.observacao())
                .usuarioId(usuarioId)
                .dataCad(LocalDate.now())
                .build();
        pagamentoDespesaRepository.save(pagamento);

        if (novoTotal.compareTo(conta.getValor()) == 0) {
            conta.setStatus(StatusConta.PAGA);
            conta.setDataBaixa(dto.dataPagamento());
            conta.setUsuarioBaixaId(usuarioId);
        }
        return contaPagarRepository.save(conta);
    }

    public List<PagamentoDespesa> listarPagamentos(Long contaPagarId) {
        buscarContaPagar(contaPagarId);
        return pagamentoDespesaRepository.findByContaPagarIdOrderByDataPagamentoAscIdAsc(contaPagarId);
    }

    public BigDecimal totalPago(Long contaPagarId) {
        BigDecimal total = pagamentoDespesaRepository.totalPagoPorConta(contaPagarId);
        return total == null ? BigDecimal.ZERO : total;
    }

    // ------ CONTAS A RECEBER ------
    public List<ContaReceber> listarContasReceber() {
        return contaReceberRepository.findAll();
    }

    public ContaReceber buscarContaReceber(Long id) {
        return contaReceberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a receber", id));
    }

    @Transactional
    public ContaReceber salvarContaReceber(ContaReceber conta) {
        if (conta.getDataCad() == null) conta.setDataCad(LocalDate.now());
        if (conta.getStatus() == null) conta.setStatus(StatusConta.PENDENTE);
        return contaReceberRepository.save(conta);
    }

    @Transactional
    public ContaReceber atualizarContaReceber(Long id, ContaReceber dados) {
        ContaReceber conta = buscarContaReceber(id);
        if (conta.getStatus() == StatusConta.CANCELADA) {
            throw new BusinessException("Conta cancelada não pode ser alterada.");
        }
        if (conta.getStatus() == StatusConta.PAGA) {
            throw new BusinessException("Conta já recebida não pode ser alterada. Registre um ajuste.");
        }

        conta.setDescricao(dados.getDescricao());
        conta.setValor(dados.getValor());
        conta.setVencimento(dados.getVencimento());
        conta.setFrequencia(dados.getFrequencia());
        conta.setContribuinte(dados.getContribuinte());
        conta.setCategoriaId(dados.getCategoriaId());
        conta.setFormaPagamento(dados.getFormaPagamento());
        conta.setObservacao(dados.getObservacao());
        return contaReceberRepository.save(conta);
    }

    @Transactional
    public ContaReceber receberConta(Long id) {
        ContaReceber conta = buscarContaReceber(id);
        if (conta.getStatus() == StatusConta.CANCELADA) {
            throw new BusinessException("Conta cancelada não pode ser recebida.");
        }
        conta.setStatus(StatusConta.PAGA);
        conta.setDataRecebimento(LocalDate.now());
        return contaReceberRepository.save(conta);
    }

    @Transactional
    public ContaReceber alterarStatusContaReceber(Long id, StatusConta novoStatus) {
        ContaReceber conta = buscarContaReceber(id);
        if (novoStatus == StatusConta.CANCELADA) {
            throw new BusinessException("Para cancelar, use o cancelamento com justificativa.");
        }
        if (conta.getStatus() == StatusConta.CANCELADA) {
            throw new BusinessException("Conta cancelada não pode ter o status alterado.");
        }
        if (conta.getStatus() == novoStatus) {
            return conta;
        }

        if (novoStatus == StatusConta.PAGA) {
            conta.setStatus(StatusConta.PAGA);
            if (conta.getDataRecebimento() == null) {
                conta.setDataRecebimento(LocalDate.now());
            }
        } else {
            conta.setStatus(novoStatus);
            conta.setDataRecebimento(null);
        }

        return contaReceberRepository.save(conta);
    }

    @Transactional
    public ContaReceber cancelarContaReceber(Long id, String justificativa, Long usuarioId) {
        ContaReceber conta = buscarContaReceber(id);
        if (conta.getStatus() == StatusConta.CANCELADA) {
            throw new BusinessException("Conta já está cancelada.");
        }
        if (conta.getStatus() == StatusConta.PAGA) {
            throw new BusinessException("Conta já recebida não pode ser cancelada.");
        }
        String motivo = justificativa == null ? "" : justificativa.trim();
        if (motivo.isEmpty()) {
            throw new BusinessException("Informe a justificativa do cancelamento.");
        }
        conta.setStatus(StatusConta.CANCELADA);
        conta.setJustificativaCancelamento(motivo);
        conta.setUsuarioCancelamentoId(usuarioId);
        conta.setDataCancelamento(LocalDate.now());
        return contaReceberRepository.save(conta);
    }

    @Transactional
    public void deletarContaReceber(Long id) {
        ContaReceber conta = contaReceberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a receber", id));
        if (conta.getStatus() != StatusConta.PENDENTE) {
            throw new BusinessException(
                    "Somente conta pendente pode ser excluída. Use o cancelamento para preservar o histórico.");
        }
        contaReceberRepository.delete(conta);
    }

    private void aplicarCancelamento(ContaPagar conta, String justificativa, Long usuarioId) {
        String motivo = justificativa == null ? "" : justificativa.trim();
        if (motivo.isEmpty()) {
            throw new BusinessException("Informe a justificativa do cancelamento.");
        }
        conta.setStatus(StatusConta.CANCELADA);
        conta.setJustificativaCancelamento(motivo);
        conta.setUsuarioCancelamentoId(usuarioId);
        conta.setDataCancelamento(LocalDate.now());
    }
}
