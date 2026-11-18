package com.sg.financeiro;

import com.sg.financeiro.dto.DoacaoRequestDTO;
import com.sg.membros.Membro;
import com.sg.membros.MembroRepository;
import com.sg.shared.enums.FormaPagamento;
import com.sg.shared.enums.StatusDoacao;
import com.sg.shared.enums.TipoDoacao;
import com.sg.shared.exceptions.BusinessException;
import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DoacaoService {

    private final DoacaoRepository doacaoRepository;
    private final MembroRepository membroRepository;

    public DoacaoService(DoacaoRepository doacaoRepository, MembroRepository membroRepository) {
        this.doacaoRepository = doacaoRepository;
        this.membroRepository = membroRepository;
    }

    /** Sobrecarga mantida para compatibilidade com chamadas existentes. */
    public List<Doacao> listar(TipoDoacao tipo, LocalDate inicio, LocalDate fim) {
        return listar(tipo, inicio, fim, null, null);
    }

    public List<Doacao> listar(TipoDoacao tipo, LocalDate inicio, LocalDate fim,
                               FormaPagamento formaPagamento, Long categoriaId) {
        return doacaoRepository.filtrar(tipo, inicio, fim, formaPagamento, categoriaId);
    }

    public Doacao buscarPorId(Long id) {
        return doacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doação", id));
    }

    /** Sobrecarga mantida para compatibilidade com chamadas existentes. */
    @Transactional
    public Doacao salvar(DoacaoRequestDTO dto) {
        return salvar(dto, null);
    }

    @Transactional
    public Doacao salvar(DoacaoRequestDTO dto, Long usuarioId) {
        Doacao doacao = Doacao.builder()
                .tipo(dto.tipo())
                .dataDoacao(dto.dataDoacao())
                .formaPagamento(dto.formaPagamento())
                .valor(dto.valor())
                .descricao(dto.descricao())
                .categoriaId(dto.categoriaId())
                .usuarioCadId(usuarioId)
                .dataCad(LocalDate.now())
                .status(StatusDoacao.ATIVA)
                .build();

        aplicarDoador(doacao, dto);
        return doacaoRepository.save(doacao);
    }

    @Transactional
    public Doacao atualizar(Long id, DoacaoRequestDTO dto) {
        Doacao doacao = buscarPorId(id);
        if (doacao.getStatus() == StatusDoacao.CANCELADA) {
            throw new BusinessException("Doação cancelada não pode ser alterada.");
        }

        doacao.setTipo(dto.tipo());
        doacao.setDataDoacao(dto.dataDoacao());
        doacao.setFormaPagamento(dto.formaPagamento());
        doacao.setValor(dto.valor());
        doacao.setDescricao(dto.descricao());
        doacao.setCategoriaId(dto.categoriaId());
        doacao.setMembroId(null);
        doacao.setNomeDoador(null);

        aplicarDoador(doacao, dto);
        return doacaoRepository.save(doacao);
    }

    @Transactional
    public Doacao cancelar(Long id, String justificativa) {
        Doacao doacao = buscarPorId(id);
        if (doacao.getStatus() == StatusDoacao.CANCELADA) {
            throw new BusinessException("Doação já está cancelada.");
        }
        String motivo = justificativa == null ? "" : justificativa.trim();
        if (motivo.isEmpty()) {
            throw new BusinessException("Informe a justificativa do cancelamento.");
        }
        doacao.setStatus(StatusDoacao.CANCELADA);
        doacao.setJustificativaCancelamento(motivo);
        return doacaoRepository.save(doacao);
    }

    /**
     * Exclusão física restrita a registro já cancelado: doação ativa é movimentação
     * confirmada e sai do histórico apenas por cancelamento.
     */
    @Transactional
    public void deletar(Long id) {
        Doacao doacao = buscarPorId(id);
        if (doacao.getStatus() != StatusDoacao.CANCELADA) {
            throw new BusinessException(
                    "Somente doação cancelada pode ser excluída. Cancele a doação para preservar o histórico.");
        }
        doacaoRepository.delete(doacao);
    }

    /**
     * Oferta é identificada por nome livre; dízimo e primícia exigem membro cadastrado.
     * O nome do membro é copiado para nomeDoador para a listagem não precisar de join.
     */
    private void aplicarDoador(Doacao doacao, DoacaoRequestDTO dto) {
        if (dto.tipo() == TipoDoacao.OFERTA) {
            String nome = dto.nomeDoador() == null ? "" : dto.nomeDoador().trim();
            if (nome.isEmpty()) {
                throw new BusinessException("Informe o nome de quem ofertou.");
            }
            doacao.setMembroId(null);
            doacao.setNomeDoador(nome);
            return;
        }

        if (dto.membroId() == null) {
            throw new BusinessException("Selecione o membro que doou.");
        }

        Membro membro = membroRepository.findById(dto.membroId())
                .orElseThrow(() -> new ResourceNotFoundException("Membro", dto.membroId()));

        doacao.setMembroId(membro.getId());
        doacao.setNomeDoador(membro.getNome());
    }
}
