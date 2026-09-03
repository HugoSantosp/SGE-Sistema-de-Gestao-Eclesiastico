package com.sg.escala;

import com.sg.escala.dto.*;
import com.sg.membros.Membro;
import com.sg.membros.MembroRepository;
import com.sg.shared.exceptions.BusinessException;
import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EscalaService {

    public static final List<String> INSTRUMENTOS = List.of(
            "Ministro", "Guitarra", "Violão", "Baixo",
            "Bateria", "Teclado", "Sax", "Backing Vocal"
    );

    private final EscalaRepository escalaRepository;
    private final EscalaDataRepository escalaDataRepository;
    private final EscalaConfirmacaoRepository confirmacaoRepository;
    private final EscalaDesignacaoRepository designacaoRepository;
    private final EscalaMusicaRepository musicaRepository;
    private final MembroRepository membroRepository;

    public EscalaService(EscalaRepository escalaRepository,
                         EscalaDataRepository escalaDataRepository,
                         EscalaConfirmacaoRepository confirmacaoRepository,
                         EscalaDesignacaoRepository designacaoRepository,
                         EscalaMusicaRepository musicaRepository,
                         MembroRepository membroRepository) {
        this.escalaRepository = escalaRepository;
        this.escalaDataRepository = escalaDataRepository;
        this.confirmacaoRepository = confirmacaoRepository;
        this.designacaoRepository = designacaoRepository;
        this.musicaRepository = musicaRepository;
        this.membroRepository = membroRepository;
    }

    // ===== ESCALAS (CRUD) =====

    @Transactional(readOnly = true)
    public List<EscalaResponseDTO> listar() {
        return escalaRepository.findAll().stream()
                .sorted(Comparator.comparing(Escala::getCreatedAt).reversed())
                .map(e -> EscalaResponseDTO.fromEntity(
                        e,
                        e.getDatas().size(),
                        e.getConfirmacoes().size()
                )).toList();
    }

    @Transactional(readOnly = true)
    public EscalaDetalhadaResponseDTO buscarPorId(Long id) {
        Escala escala = escalaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Escala", id));

        var datas = escalaDataRepository.findByEscalaIdOrderByDataAscHorarioAsc(id);
        var designacoes = carregarDesignacoes(datas);
        var musicas = carregarMusicas(datas);

        return EscalaDetalhadaResponseDTO.fromEntity(escala, datas, designacoes, musicas);
    }

    @Transactional
    public EscalaResponseDTO criar(EscalaRequestDTO dto) {
        var escala = Escala.builder()
                .titulo(dto.titulo())
                .ministerioId(dto.ministerioId())
                .publicToken(UUID.randomUUID().toString().replace("-", ""))
                .aberta(true)
                .build();
        escala = escalaRepository.save(escala);
        return EscalaResponseDTO.fromEntity(escala, 0, 0);
    }

    @Transactional
    public void toggleEscala(Long id) {
        var escala = escalaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Escala", id));
        escala.setAberta(!escala.isAberta());
        escalaRepository.save(escala);
    }

    @Transactional
    public void deletar(Long id) {
        if (!escalaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Escala", id);
        }
        escalaRepository.deleteById(id);
    }

    @Transactional
    public String gerarLinkResultado(Long id) {
        var escala = escalaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Escala", id));
        if (escala.getResultadoToken() == null) {
            escala.setResultadoToken(UUID.randomUUID().toString().replace("-", ""));
            escalaRepository.save(escala);
        }
        return escala.getResultadoToken();
    }

    // ===== DATAS =====

    @Transactional
    public EscalaDataDTO adicionarData(Long escalaId, EscalaDataDTO dto) {
        var escala = escalaRepository.findById(escalaId)
                .orElseThrow(() -> new ResourceNotFoundException("Escala", escalaId));

        var data = EscalaData.builder()
                .escala(escala)
                .nomeEvento(dto.nomeEvento())
                .data(dto.data())
                .horario(dto.horario())
                .local(dto.local())
                .build();
        data = escalaDataRepository.save(data);
        return EscalaDataDTO.fromEntity(data);
    }

    @Transactional
    public void removerData(Long escalaId, Long dataId) {
        var data = escalaDataRepository.findById(dataId)
                .orElseThrow(() -> new ResourceNotFoundException("Data da escala", dataId));
        if (!data.getEscala().getId().equals(escalaId)) {
            throw new BusinessException("Data não pertence a esta escala");
        }
        escalaDataRepository.delete(data);
    }

    // ===== DESIGNAÇÕES (montagem) =====

    @Transactional
    public void salvarDesignacoes(Long escalaId, Long dataId, List<EscalaDesignacaoRequestDTO> designacoes) {
        var data = escalaDataRepository.findById(dataId)
                .orElseThrow(() -> new ResourceNotFoundException("Data da escala", dataId));
        if (!data.getEscala().getId().equals(escalaId)) {
            throw new BusinessException("Data não pertence a esta escala");
        }

        // Remove designações antigas desta data
        designacaoRepository.deleteByEscalaDataId(dataId);

        // Salva as novas
        for (int i = 0; i < designacoes.size(); i++) {
            var d = designacoes.get(i);
            var confirmacao = confirmacaoRepository.findById(d.confirmacaoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Confirmação", d.confirmacaoId()));

            designacaoRepository.save(EscalaDesignacao.builder()
                    .escalaData(data)
                    .confirmacao(confirmacao)
                    .instrumento(d.instrumento())
                    .ordem(d.ordem() != null ? d.ordem() : i)
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public List<EscalaDesignacaoResponseDTO> listarDesignacoes(Long dataId) {
        return designacaoRepository.findByEscalaDataId(dataId).stream()
                .map(EscalaDesignacaoResponseDTO::fromEntity)
                .toList();
    }

    // ===== MÚSICAS (repertório) =====

    @Transactional
    public void salvarMusicas(Long escalaId, Long dataId, List<EscalaMusicaDTO> musicas) {
        var data = escalaDataRepository.findById(dataId)
                .orElseThrow(() -> new ResourceNotFoundException("Data da escala", dataId));
        if (!data.getEscala().getId().equals(escalaId)) {
            throw new BusinessException("Data não pertence a esta escala");
        }

        musicaRepository.deleteByEscalaDataId(dataId);

        for (int i = 0; i < musicas.size(); i++) {
            var m = musicas.get(i);
            musicaRepository.save(EscalaMusica.builder()
                    .escalaData(data)
                    .nome(m.nome())
                    .artista(m.artista())
                    .link(m.link())
                    .ordem(m.ordem() != null ? m.ordem() : i)
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public List<EscalaMusicaDTO> listarMusicas(Long dataId) {
        return musicaRepository.findByEscalaDataIdOrderByOrdemAsc(dataId).stream()
                .map(EscalaMusicaDTO::fromEntity)
                .toList();
    }

    /** Salva músicas usando o resultado_token (público, sem JWT) */
    @Transactional
    public void salvarMusicasPorToken(String resultadoToken, Long dataId, List<EscalaMusicaDTO> musicas) {
        var escala = escalaRepository.findByResultadoToken(resultadoToken)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado não encontrado"));

        var data = escalaDataRepository.findById(dataId)
                .orElseThrow(() -> new ResourceNotFoundException("Data da escala", dataId));
        if (!data.getEscala().getId().equals(escala.getId())) {
            throw new BusinessException("Data não pertence a esta escala");
        }

        musicaRepository.deleteByEscalaDataId(dataId);

        for (int i = 0; i < musicas.size(); i++) {
            var m = musicas.get(i);
            musicaRepository.save(EscalaMusica.builder()
                    .escalaData(data)
                    .nome(m.nome())
                    .artista(m.artista())
                    .link(m.link())
                    .ordem(m.ordem() != null ? m.ordem() : i)
                    .build());
        }
    }

    // ===== PÚBLICO (links com token) =====

    @Transactional(readOnly = true)
    public EscalaDetalhadaResponseDTO buscarPorTokenPublico(String token) {
        var escala = escalaRepository.findByPublicTokenAndAbertaTrue(token)
                .orElseThrow(() -> new ResourceNotFoundException("Escala não encontrada ou fechada"));

        var datas = escalaDataRepository.findByEscalaIdOrderByDataAscHorarioAsc(escala.getId());
        // Não expõe confirmações (nome/email/celular) no link público — privacidade
        return EscalaDetalhadaResponseDTO.fromEntityPublica(escala, datas, Map.of(), Map.of());
    }

    @Transactional
    public EscalaConfirmacaoResponseDTO confirmarDisponibilidade(String token, EscalaConfirmacaoRequestDTO dto) {
        var escala = escalaRepository.findByPublicTokenAndAbertaTrue(token)
                .orElseThrow(() -> new ResourceNotFoundException("Escala não encontrada ou fechada"));

        // Tenta vincular a um membro existente (igreja única)
        Membro membro = null;
        List<Membro> membrosEncontrados = membroRepository.findByNomeContainingIgnoreCase(dto.nome());
        if (!membrosEncontrados.isEmpty()) {
            membro = membrosEncontrados.get(0);
        }

        var confirmacao = EscalaConfirmacao.builder()
                .escala(escala)
                .membro(membro)
                .nome(dto.nome())
                .email(dto.email() != null ? dto.email() : null)
                .celular(dto.celular() != null ? dto.celular() : (membro != null ? membro.getTelefone() : null))
                .build();
        confirmacao = confirmacaoRepository.save(confirmacao);

        // Vincula as datas selecionadas
        var datas = escalaDataRepository.findAllById(dto.dataIds());
        confirmacao.setDatas(datas);
        confirmacaoRepository.save(confirmacao);

        return EscalaConfirmacaoResponseDTO.fromEntity(confirmacao, dto.dataIds());
    }

    @Transactional(readOnly = true)
    public EscalaDetalhadaResponseDTO buscarResultadoPorToken(String token) {
        var escala = escalaRepository.findByResultadoToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado não encontrado"));

        var datas = escalaDataRepository.findByEscalaIdOrderByDataAscHorarioAsc(escala.getId());
        var designacoes = carregarDesignacoes(datas);
        var musicas = carregarMusicas(datas);

        // Não expõe confirmações (nome/email/celular) em links públicos — privacidade
        return EscalaDetalhadaResponseDTO.fromEntityPublica(escala, datas, designacoes, musicas);
    }

    // ===== HELPERS =====

    private Map<Long, List<EscalaDesignacao>> carregarDesignacoes(List<EscalaData> datas) {
        if (datas.isEmpty()) return Map.of();
        return designacaoRepository.findByEscalaDataEscalaId(datas.get(0).getEscala().getId())
                .stream()
                .collect(Collectors.groupingBy(d -> d.getEscalaData().getId()));
    }

    private Map<Long, List<EscalaMusica>> carregarMusicas(List<EscalaData> datas) {
        if (datas.isEmpty()) return Map.of();
        return datas.stream().collect(Collectors.toMap(
                EscalaData::getId,
                d -> musicaRepository.findByEscalaDataIdOrderByOrdemAsc(d.getId())
        ));
    }
}
