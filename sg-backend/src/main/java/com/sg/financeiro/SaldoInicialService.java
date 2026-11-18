package com.sg.financeiro;

import com.sg.financeiro.dto.SaldoInicialRequestDTO;
import com.sg.shared.exceptions.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SaldoInicialService {

    private final SaldoInicialRepository saldoInicialRepository;

    public SaldoInicialService(SaldoInicialRepository saldoInicialRepository) {
        this.saldoInicialRepository = saldoInicialRepository;
    }

    public List<SaldoInicial> historico() {
        return saldoInicialRepository.findAllByOrderByDataReferenciaDescIdDesc();
    }

    /** Saldo vigente; ZERO quando nunca foi configurado. */
    public BigDecimal saldoVigente() {
        return saldoInicialRepository.findTopByOrderByDataReferenciaDescIdDesc()
                .map(SaldoInicial::getValor)
                .orElse(BigDecimal.ZERO);
    }

    public SaldoInicial vigente() {
        return saldoInicialRepository.findTopByOrderByDataReferenciaDescIdDesc().orElse(null);
    }

    /**
     * Não existe alteração silenciosa: cada chamada registra um novo saldo com
     * data de referência, usuário e observação.
     */
    @Transactional
    public SaldoInicial registrar(SaldoInicialRequestDTO dto, Long usuarioId) {
        if (dto.valor() == null) {
            throw new BusinessException("Informe o valor do saldo inicial.");
        }
        if (dto.dataReferencia() == null) {
            throw new BusinessException("Informe a data de referência do saldo inicial.");
        }

        SaldoInicial saldo = SaldoInicial.builder()
                .valor(dto.valor())
                .dataReferencia(dto.dataReferencia())
                .observacao(dto.observacao())
                .usuarioId(usuarioId)
                .dataCad(LocalDate.now())
                .build();

        return saldoInicialRepository.save(saldo);
    }
}
