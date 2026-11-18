package com.sg.financeiro;

import com.sg.financeiro.dto.CategoriaRequestDTO;
import com.sg.shared.enums.TipoCategoria;
import com.sg.shared.exceptions.BusinessException;
import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CategoriaFinanceiraService {

    private final CategoriaFinanceiraRepository categoriaRepository;
    private final ContaPagarRepository contaPagarRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final DoacaoRepository doacaoRepository;

    public CategoriaFinanceiraService(CategoriaFinanceiraRepository categoriaRepository,
                                      ContaPagarRepository contaPagarRepository,
                                      ContaReceberRepository contaReceberRepository,
                                      DoacaoRepository doacaoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.contaPagarRepository = contaPagarRepository;
        this.contaReceberRepository = contaReceberRepository;
        this.doacaoRepository = doacaoRepository;
    }

    public List<CategoriaFinanceira> listar(TipoCategoria tipo, Boolean apenasAtivas) {
        if (tipo != null && Boolean.TRUE.equals(apenasAtivas)) {
            return categoriaRepository.findByTipoAndAtivoTrueOrderByNomeAsc(tipo);
        }
        if (tipo != null) {
            return categoriaRepository.findByTipoOrderByNomeAsc(tipo);
        }
        return categoriaRepository.findAll();
    }

    public CategoriaFinanceira buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria financeira", id));
    }

    @Transactional
    public CategoriaFinanceira criar(CategoriaRequestDTO dto) {
        String nome = normalizarNome(dto.nome());
        if (categoriaRepository.existsByNomeIgnoreCaseAndTipo(nome, dto.tipo())) {
            throw new BusinessException("Já existe uma categoria '" + nome + "' para este tipo.");
        }

        CategoriaFinanceira categoria = CategoriaFinanceira.builder()
                .nome(nome)
                .tipo(dto.tipo())
                .ativo(dto.ativo() == null || dto.ativo())
                .dataCad(LocalDate.now())
                .build();

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public CategoriaFinanceira atualizar(Long id, CategoriaRequestDTO dto) {
        CategoriaFinanceira categoria = buscarPorId(id);
        String nome = normalizarNome(dto.nome());

        boolean mudouTipo = categoria.getTipo() != dto.tipo();
        if (mudouTipo && possuiVinculo(id)) {
            throw new BusinessException(
                    "Categoria com movimentações vinculadas não pode mudar de tipo. Desative-a e crie outra.");
        }

        if (!categoria.getNome().equalsIgnoreCase(nome)
                && categoriaRepository.existsByNomeIgnoreCaseAndTipo(nome, dto.tipo())) {
            throw new BusinessException("Já existe uma categoria '" + nome + "' para este tipo.");
        }

        categoria.setNome(nome);
        categoria.setTipo(dto.tipo());
        if (dto.ativo() != null) {
            categoria.setAtivo(dto.ativo());
        }

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public CategoriaFinanceira alterarAtivo(Long id, boolean ativo) {
        CategoriaFinanceira categoria = buscarPorId(id);
        categoria.setAtivo(ativo);
        return categoriaRepository.save(categoria);
    }

    /**
     * Categoria vinculada a movimentação não é excluída: o histórico financeiro
     * perderia a classificação. Nesse caso a saída é desativar.
     */
    @Transactional
    public void deletar(Long id) {
        CategoriaFinanceira categoria = buscarPorId(id);
        if (possuiVinculo(id)) {
            throw new BusinessException(
                    "Categoria '" + categoria.getNome() + "' está vinculada a movimentações e não pode ser excluída. "
                            + "Desative-a para impedir novos lançamentos.");
        }
        categoriaRepository.delete(categoria);
    }

    public boolean possuiVinculo(Long categoriaId) {
        return contaPagarRepository.existsByCategoriaId(categoriaId)
                || contaReceberRepository.existsByCategoriaId(categoriaId)
                || doacaoRepository.existsByCategoriaId(categoriaId);
    }

    private String normalizarNome(String nome) {
        String limpo = nome == null ? "" : nome.trim();
        if (limpo.isEmpty()) {
            throw new BusinessException("Informe o nome da categoria.");
        }
        return limpo;
    }
}
