package com.sg.escala;

import com.sg.escala.dto.*;
import com.sg.membros.Membro;
import com.sg.membros.MembroRepository;
import com.sg.shared.exceptions.BusinessException;
import com.sg.shared.exceptions.ResourceNotFoundException;
import com.sg.shared.services.SignedUrlService;
import com.sg.usuario.Usuario;
import com.sg.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EscalaService {

    private static final Logger logger = LoggerFactory.getLogger(EscalaService.class);

    /**
     * Funções universais para designação em escalas (não apenas músicos).
     * Ainda é possível informar uma função livre via opção "Outro…" no app.
     */
    public static final List<String> INSTRUMENTOS = List.of(
            // Louvor
            "Ministro", "Vocalista", "Backing Vocal",
            "Guitarra", "Violão", "Baixo", "Bateria", "Teclado", "Sax",
            // Técnica e mídia
            "Técnico de Som", "Projeção", "Câmera/Live", "Iluminação",
            // Apoio e serviço
            "Recepção", "Intercessão", "Diaconato", "Crianças"
    );

    private final EscalaRepository escalaRepository;
    private final EscalaDataRepository escalaDataRepository;
    private final EscalaConfirmacaoRepository confirmacaoRepository;
    private final EscalaDesignacaoRepository designacaoRepository;
    private final EscalaMusicaRepository musicaRepository;
    private final MembroRepository membroRepository;
    private final UsuarioRepository usuarioRepository;
    private final SignedUrlService signedUrlService;

    public EscalaService(EscalaRepository escalaRepository,
                         EscalaDataRepository escalaDataRepository,
                         EscalaConfirmacaoRepository confirmacaoRepository,
                         EscalaDesignacaoRepository designacaoRepository,
                         EscalaMusicaRepository musicaRepository,
                         MembroRepository membroRepository,
                         UsuarioRepository usuarioRepository,
                         SignedUrlService signedUrlService) {
        this.escalaRepository = escalaRepository;
        this.escalaDataRepository = escalaDataRepository;
        this.confirmacaoRepository = confirmacaoRepository;
        this.designacaoRepository = designacaoRepository;
        this.musicaRepository = musicaRepository;
        this.membroRepository = membroRepository;
        this.usuarioRepository = usuarioRepository;
        this.signedUrlService = signedUrlService;
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
        var designacoes = designacaoRepository.findByEscalaDataId(dataId);
        var fotos = fotosPorConfirmacao(Map.of(dataId, designacoes));
        return designacoes.stream()
                .map(d -> EscalaDesignacaoResponseDTO.fromEntity(
                        d, fotos.getOrDefault(d.getConfirmacao().getId(), "")))
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
        Membro membro = resolverMembro(dto.nome(), dto.email());

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

    /**
     * Resolve o membro da igreja a partir do nome/e-mail informados no link público.
     *
     * Ordem: nome exato (ignorando caixa) → nome contido → e-mail de usuário vinculado
     * a um membro. O vínculo por nome contido pode casar com a pessoa errada quando há
     * nomes parecidos, por isso o nome exato vem primeiro.
     */
    private Membro resolverMembro(String nome, String email) {
        if (nome != null && !nome.isBlank()) {
            String alvo = nome.trim();
            List<Membro> encontrados = membroRepository.findByNomeContainingIgnoreCase(alvo);
            Optional<Membro> exato = encontrados.stream()
                    .filter(m -> m.getNome() != null && m.getNome().trim().equalsIgnoreCase(alvo))
                    .findFirst();
            if (exato.isPresent()) {
                return exato.get();
            }
            if (!encontrados.isEmpty()) {
                return encontrados.get(0);
            }
        }
        if (email != null && !email.isBlank()) {
            return usuarioRepository.findByEmail(email.trim())
                    .map(Usuario::getIdPessoa)
                    .flatMap(membroRepository::findById)
                    .orElse(null);
        }
        return null;
    }

    /**
     * Resolve o membro a partir de um usuário: primeiro pelo idPessoa e, se ausente,
     * pelo documento (vínculo canônico do sistema). Retorna null se não houver membro.
     */
    private Membro membroDoUsuario(Usuario usuario) {
        if (usuario.getIdPessoa() != null) {
            Optional<Membro> porIdPessoa = membroRepository.findById(usuario.getIdPessoa());
            if (porIdPessoa.isPresent()) {
                return porIdPessoa.get();
            }
        }
        String documento = usuario.getDocumento();
        if (documento != null && !documento.isBlank()) {
            return membroRepository.findByDocumentoIgnoreCase(documento.trim()).stream()
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public EscalaDetalhadaResponseDTO buscarResultadoPorToken(String token) {
        var escala = escalaRepository.findByResultadoToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado não encontrado"));

        var datas = escalaDataRepository.findByEscalaIdOrderByDataAscHorarioAsc(escala.getId());
        var designacoes = carregarDesignacoes(datas);
        var musicas = carregarMusicas(datas);

        // Não expõe confirmações (nome/email/celular) em links públicos — privacidade
        var fotos = fotosPorConfirmacao(designacoes);
        return EscalaDetalhadaResponseDTO.fromEntityPublica(escala, datas, designacoes, musicas, fotos);
    }

    // ===== HELPERS =====

    private Map<Long, List<EscalaDesignacao>> carregarDesignacoes(List<EscalaData> datas) {
        if (datas.isEmpty()) return Map.of();
        return designacaoRepository.findByEscalaDataEscalaId(datas.get(0).getEscala().getId())
                .stream()
                .collect(Collectors.groupingBy(d -> d.getEscalaData().getId()));
    }

    /**
     * Mapa confirmaçãoId → URL assinada da foto do integrante.
     *
     * A foto enviada pelo próprio usuário no Meu Ministério fica em usuarios.foto;
     * a foto do cadastro administrativo fica em membros.foto. Damos preferência à
     * primeira e usamos a segunda como compatibilidade com cadastros antigos.
     * Sem foto em nenhuma das duas, o mapa não recebe a chave e o frontend usa o ícone.
     */
    private Map<Long, String> fotosPorConfirmacao(Map<Long, List<EscalaDesignacao>> designacoesPorData) {
        Map<Long, String> fotos = new HashMap<>();
        Map<Long, String> cachePorMembro = new HashMap<>();
        int total = 0;
        int semMembro = 0;
        for (List<EscalaDesignacao> lista : designacoesPorData.values()) {
            for (EscalaDesignacao d : lista) {
                total++;
                var confirmacao = d.getConfirmacao();
                if (confirmacao == null || fotos.containsKey(confirmacao.getId())) {
                    continue;
                }
                Membro membro = confirmacao.getMembro();
                if (membro == null || membro.getId() == null) {
                    // Confirmações antigas sem vínculo: tenta pela foto do usuário com o mesmo nome
                    semMembro++;
                    String caminho = caminhoFotoPorNome(confirmacao.getNome());
                    if (caminho != null && !caminho.isBlank()) {
                        fotos.put(confirmacao.getId(), signedUrlService.signStoredPath(caminho));
                    }
                    continue;
                }
                String caminho = cachePorMembro.computeIfAbsent(membro.getId(), id -> {
                    String c = caminhoFoto(membro);
                    return c == null ? "" : c;
                });
                if (caminho != null && !caminho.isBlank()) {
                    fotos.put(confirmacao.getId(), signedUrlService.signStoredPath(caminho));
                }
            }
        }
        logger.info("Fotos de integrantes: {} resolvida(s) de {} designação(ões) ({} sem membro vinculado)",
                fotos.size(), total, semMembro);
        return fotos;
    }

    /**
     * Último recurso para confirmações sem membro: procura um usuário com o mesmo nome
     * que tenha foto. Só aceita quando há exatamente um candidato, ou quando todos os
     * homônimos apontam para o mesmo membro, para não exibir a foto de outra pessoa.
     */
    private String caminhoFotoPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        String alvo = nome.trim();
        List<Usuario> candidatos = usuarioRepository.findByNomeContainingIgnoreCase(alvo).stream()
                .filter(u -> u.getNome() != null && u.getNome().trim().equalsIgnoreCase(alvo))
                .filter(u -> u.getFoto() != null && !u.getFoto().isBlank())
                .toList();
        if (candidatos.size() == 1) {
            return candidatos.get(0).getFoto();
        }
        // Homônimos: aceita apenas se todos apontarem para o mesmo membro
        if (candidatos.size() > 1) {
            var membros = candidatos.stream()
                    .map(this::membroDoUsuario)
                    .filter(m -> m != null && m.getId() != null)
                    .map(Membro::getId)
                    .distinct()
                    .toList();
            if (membros.size() == 1) {
                return candidatos.get(0).getFoto();
            }
        }
        return null;
    }

    /** Foto do integrante: prioriza usuarios.foto (enviada no app) e usa membros.foto como fallback. */
    private String caminhoFoto(Membro membro) {
        if (membro == null || membro.getId() == null) {
            return null;
        }
        String doUsuario = usuarioDoMembro(membro)
                .map(Usuario::getFoto)
                .filter(f -> f != null && !f.isBlank())
                .orElse(null);
        if (doUsuario != null) {
            return doUsuario;
        }
        return membro.getFoto();
    }

    /**
     * Usuário dono do cadastro de membro.
     *
     * O vínculo canônico no sistema é o documento (mesmo critério usado em
     * UsuarioService.criarOuAtualizarCargo e MeuMinisterioService). O idPessoa é
     * usado como alternativa para cadastros antigos que ainda não o têm preenchido.
     * O documento pode estar duplicado em vários usuários, por isso a busca é por lista.
     */
    private Optional<Usuario> usuarioDoMembro(Membro membro) {
        String documento = membro.getDocumento();
        if (documento != null && !documento.isBlank()) {
            // findByDocumento exige resultado único; o documento pode estar duplicado
            // em usuários distintos, então usamos a lista e escolhemos com foto.
            List<Usuario> porDocumento = usuarioRepository.findByDocumentoIgnoreCase(documento.trim());
            Optional<Usuario> comFoto = porDocumento.stream()
                    .filter(u -> u.getFoto() != null && !u.getFoto().isBlank())
                    .findFirst();
            if (comFoto.isPresent()) {
                return comFoto;
            }
            if (!porDocumento.isEmpty()) {
                return Optional.of(porDocumento.get(0));
            }
        }
        return usuarioRepository.findFirstByIdPessoa(membro.getId());
    }

    private Map<Long, List<EscalaMusica>> carregarMusicas(List<EscalaData> datas) {
        if (datas.isEmpty()) return Map.of();
        return datas.stream().collect(Collectors.toMap(
                EscalaData::getId,
                d -> musicaRepository.findByEscalaDataIdOrderByOrdemAsc(d.getId())
        ));
    }
}
