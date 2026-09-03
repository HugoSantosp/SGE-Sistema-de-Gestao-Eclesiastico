package com.sg.financeiro;

import com.sg.shared.enums.StatusConta;
import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class FinanceiroService {

    private final ContaPagarRepository contaPagarRepository;
    private final ContaReceberRepository contaReceberRepository;

    public FinanceiroService(ContaPagarRepository contaPagarRepository,
                             ContaReceberRepository contaReceberRepository) {
        this.contaPagarRepository = contaPagarRepository;
        this.contaReceberRepository = contaReceberRepository;
    }

    // ------ CONTAS A PAGAR ------
    public List<ContaPagar> listarContasPagar() {
        return contaPagarRepository.findAll();
    }

    public ContaPagar buscarContaPagar(Long id) {
        return contaPagarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a pagar", id));
    }

    public ContaPagar salvarContaPagar(ContaPagar conta) {
        if (conta.getDataCad() == null) conta.setDataCad(LocalDate.now());
        if (conta.getStatus() == null) conta.setStatus(StatusConta.PENDENTE);
        return contaPagarRepository.save(conta);
    }

    public ContaPagar baixarContaPagar(Long id) {
        ContaPagar conta = buscarContaPagar(id);
        conta.setStatus(StatusConta.PAGA);
        conta.setDataBaixa(LocalDate.now());
        return contaPagarRepository.save(conta);
    }

    public void deletarContaPagar(Long id) {
        if (!contaPagarRepository.existsById(id))
            throw new ResourceNotFoundException("Conta a pagar", id);
        contaPagarRepository.deleteById(id);
    }

    // ------ CONTAS A RECEBER ------
    public List<ContaReceber> listarContasReceber() {
        return contaReceberRepository.findAll();
    }

    public ContaReceber buscarContaReceber(Long id) {
        return contaReceberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta a receber", id));
    }

    public ContaReceber salvarContaReceber(ContaReceber conta) {
        if (conta.getDataCad() == null) conta.setDataCad(LocalDate.now());
        if (conta.getStatus() == null) conta.setStatus(StatusConta.PENDENTE);
        return contaReceberRepository.save(conta);
    }

    public ContaReceber receberConta(Long id) {
        ContaReceber conta = buscarContaReceber(id);
        conta.setStatus(StatusConta.PAGA);
        conta.setDataRecebimento(LocalDate.now());
        return contaReceberRepository.save(conta);
    }

    public void deletarContaReceber(Long id) {
        if (!contaReceberRepository.existsById(id))
            throw new ResourceNotFoundException("Conta a receber", id);
        contaReceberRepository.deleteById(id);
    }
}
