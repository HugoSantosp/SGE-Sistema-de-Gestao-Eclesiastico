package com.sg.meuministerio;

import com.sg.escala.Escala;
import com.sg.escala.EscalaConfirmacao;
import com.sg.escala.EscalaConfirmacaoRepository;
import com.sg.escala.EscalaDataRepository;
import com.sg.escala.EscalaRepository;
import com.sg.escala.EscalaService;
import com.sg.escala.dto.EscalaConfirmacaoResponseDTO;
import com.sg.escala.dto.EscalaDataDTO;
import com.sg.escala.dto.EscalaDesignacaoRequestDTO;
import com.sg.escala.dto.EscalaDetalhadaResponseDTO;
import com.sg.escala.dto.EscalaMusicaDTO;
import com.sg.escala.dto.EscalaRequestDTO;
import com.sg.escala.dto.EscalaResponseDTO;
import com.sg.membros.Membro;
import com.sg.membros.MembroRepository;
import com.sg.meuministerio.dto.AdicionarMembroRequestDTO;
import com.sg.meuministerio.dto.MembroMinisterioDTO;
import com.sg.meuministerio.dto.MinisterioDoUsuarioDTO;
import com.sg.ministerios.MinisterioMembro;
import com.sg.ministerios.MinisterioMembroRepository;
import com.sg.ministerios.MinisterioRepository;
import com.sg.shared.enums.NivelAcesso;
import com.sg.shared.enums.PapelMinisterio;
import com.sg.shared.exceptions.BusinessException;
import com.sg.shared.exceptions.ResourceNotFoundException;
import com.sg.usuario.Usuario;
import com.sg.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviço do app "MeuMinisterio" (estilo Voluts).
 * <p>
 * O usuário do SGE tem papéis COMPOSTOS:
 * - Papel global na igreja ({@link NivelAcesso}) — cargo eclesiástico/administrativo.
 * - Papéis por ministério ({@link PapelMinisterio}) — um em cada vínculo {@link MinisterioMembro}.
 * <p>
 * Permissões:
 * - PASTOR_PRESIDENTE / PASTOR_AUXILIAR: acesso ampliado — enxergam e gerenciam todos os ministérios.
 *   Além disso, mantêm seus vínculos de ministério (ex.: pastor que continua líder do Louvor).
 * - Qualquer usuário com idPessoa: participa dos ministérios onde possui vínculo (LIDER, INTEGRANTE,
 *   MUSICO, VOCALISTA, BACKING_VOCAL, TECNICO, OPERADOR, INTERCESSOR, RECEPCAO, FACILITADOR).
 *   Líder gerencia membros e monta escalas; os demais papéis confirmam disponibilidade e veem escalas.
 */
@Service
public class MeuMinisterioService {

    private final MinisterioRepository ministerioRepository;
    private final MinisterioMembroRepository ministerioMembroRepository;
    private final MembroRepository membroRepository;
    private final EscalaRepository escalaRepository;
    private final EscalaDataRepository escalaDataRepository;
    private final EscalaConfirmacaoRepository confirmacaoRepository;
    private final EscalaService escalaService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SENHA_TEMPORARIA = "12345678";

    public MeuMinisterioService(MinisterioRepository ministerioRepository,
                                MinisterioMembroRepository ministerioMembroRepository,
                                MembroRepository membroRepository,
                                EscalaRepository escalaRepository,
                                EscalaDataRepository escalaDataRepository,
                                EscalaConfirmacaoRepository confirmacaoRepository,
                                EscalaService escalaService,
                                UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder) {
        this.ministerioRepository = ministerioRepository;
        this.ministerioMembroRepository = ministerioMembroRepository;
        this.membroRepository = membroRepository;
        this.escalaRepository = escalaRepository;
        this.escalaDataRepository = escalaDataRepository;
        this.confirmacaoRepository = confirmacaoRepository;
        this.escalaService = escalaService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===== MINISTÉRIOS DO USUÁRIO =====

    /** Ministérios visíveis ao usuário (todos para pastor; vínculos para os demais). */
    @Transactional(readOnly = true)
    public List<MinisterioDoUsuarioDTO> listarMeusMinisterios(Usuario usuario) {
        if (isAcessoAmpliado(usuario)) {
            // Pastor enxerga todos os ministérios; papel real quando possui vínculo (papéis compostos).
            Map<Long, PapelMinisterio> papeisPorMinisterio = papeisDoUsuario(usuario);
            return ministerioRepository.findAll().stream()
                    .map(m -> new MinisterioDoUsuarioDTO(
                            m.getId(), m.getNome(), m.getDescricao(), m.getFoto(),
                            papeisPorMinisterio.get(m.getId())))
                    .toList();
        }
        Long membroId = membroDoUsuario(usuario).map(Membro::getId).orElse(null);
        if (membroId == null) return List.of();
        return ministerioMembroRepository.findByMembroId(membroId).stream()
                .map(v -> new MinisterioDoUsuarioDTO(
                        v.getMinisterio().getId(),
                        v.getMinisterio().getNome(),
                        v.getMinisterio().getDescricao(),
                        v.getMinisterio().getFoto(),
                        v.getPapel()))
                .toList();
    }

    /** Ministérios onde o usuário é líder (todos para pastor). */
    @Transactional(readOnly = true)
    public List<MinisterioDoUsuarioDTO> listarMinisteriosLiderados(Usuario usuario) {
        if (isAcessoAmpliado(usuario)) {
            return listarMeusMinisterios(usuario);
        }
        Long membroId = membroDoUsuario(usuario).map(Membro::getId).orElse(null);
        if (membroId == null) return List.of();
        return ministerioMembroRepository.findByMembroId(membroId).stream()
                .filter(v -> v.getPapel() == PapelMinisterio.LIDER)
                .map(v -> new MinisterioDoUsuarioDTO(
                        v.getMinisterio().getId(),
                        v.getMinisterio().getNome(),
                        v.getMinisterio().getDescricao(),
                        v.getMinisterio().getFoto(),
                        v.getPapel()))
                .toList();
    }

    // ===== GESTÃO DE MEMBROS (líder do ministério) =====

    @Transactional(readOnly = true)
    public List<MembroMinisterioDTO> listarMembrosDoMinisterio(Long ministerioId, Usuario usuario) {
        validarLider(usuario, ministerioId);
        return ministerioMembroRepository.findByMinisterioId(ministerioId).stream()
                .map(v -> new MembroMinisterioDTO(
                        v.getId(),
                        v.getMembro().getId(),
                        v.getMembro().getNome(),
                        v.getMembro().getDocumento(),
                        v.getMembro().getTelefone(),
                        v.getMembro().getFoto(),
                        v.getPapel()))
                .toList();
    }

    @Transactional
    public void adicionarMembro(Long ministerioId, AdicionarMembroRequestDTO dto, Usuario usuario) {
        validarLider(usuario, ministerioId);

        Membro membro = membroRepository.findById(dto.membroId())
                .orElseThrow(() -> new ResourceNotFoundException("Membro", dto.membroId()));

        var ministerio = ministerioRepository.findById(ministerioId)
                .orElseThrow(() -> new ResourceNotFoundException("Ministério", ministerioId));

        // Se o membro não tem Usuário vinculado, cria automaticamente
        if (membro.getDocumento() != null && !membro.getDocumento().isEmpty()) {
            Optional<Usuario> usuarioExistente = usuarioRepository.findByDocumento(membro.getDocumento());
            if (usuarioExistente.isEmpty()) {
                // Gera email a partir do documento
                String email = membro.getDocumento().replaceAll("[^a-zA-Z0-9]", "") + "@icert.local";
                
                // Verifica se o email já está em uso
                if (usuarioRepository.existsByEmail(email)) {
                    // Adiciona sufixo numérico para evitar conflito
                    int counter = 1;
                    while (usuarioRepository.existsByEmail(membro.getDocumento().replaceAll("[^a-zA-Z0-9]", "") + counter + "@icert.local")) {
                        counter++;
                    }
                    email = membro.getDocumento().replaceAll("[^a-zA-Z0-9]", "") + counter + "@icert.local";
                }

                Usuario novoUsuario = Usuario.builder()
                        .nome(membro.getNome())
                        .documento(membro.getDocumento())
                        .email(email)
                        .senha(passwordEncoder.encode(SENHA_TEMPORARIA))
                        .nivel(NivelAcesso.MEMBRO)
                        .senhaTemporaria(true)
                        .idPessoa(membro.getId())
                        .build();
                
                usuarioRepository.save(novoUsuario);
            } else {
                // Se já existe Usuário, garante que o idPessoa está vinculado
                Usuario usuarioEncontrado = usuarioExistente.get();
                if (usuarioEncontrado.getIdPessoa() == null || !usuarioEncontrado.getIdPessoa().equals(membro.getId())) {
                    usuarioEncontrado.setIdPessoa(membro.getId());
                    usuarioRepository.save(usuarioEncontrado);
                }
            }
        }

        // Upsert: se o vínculo já existe, apenas atualiza o papel
        MinisterioMembro vinculo = ministerioMembroRepository
                .findByMinisterioIdAndMembroId(ministerioId, dto.membroId())
                .orElse(MinisterioMembro.builder()
                        .ministerio(ministerio)
                        .membro(membro)
                        .build());
        vinculo.setPapel(dto.papel());
        ministerioMembroRepository.save(vinculo);
    }

    @Transactional
    public void alterarPapel(Long ministerioId, Long membroId, PapelMinisterio papel, Usuario usuario) {
        validarLider(usuario, ministerioId);
        var vinculo = ministerioMembroRepository.findByMinisterioIdAndMembroId(ministerioId, membroId)
                .orElseThrow(() -> new BusinessException("Membro não pertence a este ministério"));
        vinculo.setPapel(papel);
        ministerioMembroRepository.save(vinculo);
    }

    @Transactional
    public void removerMembro(Long ministerioId, Long membroId, Usuario usuario) {
        validarLider(usuario, ministerioId);
        ministerioMembroRepository.findByMinisterioIdAndMembroId(ministerioId, membroId)
                .ifPresent(ministerioMembroRepository::delete);
    }

    // ===== ESCALAS =====

    /** Escalas dos ministérios do usuário (todas para pastor). */
    @Transactional(readOnly = true)
    public List<EscalaResponseDTO> listarMinhasEscalas(Usuario usuario) {
        if (isAcessoAmpliado(usuario)) {
            return escalaService.listar();
        }
        Long membroId = membroDoUsuario(usuario).map(Membro::getId).orElse(null);
        if (membroId == null) return List.of();
        var ministerioIds = ministerioMembroRepository.findByMembroId(membroId).stream()
                .map(v -> v.getMinisterio().getId())
                .toList();
        if (ministerioIds.isEmpty()) return List.of();
        return escalaRepository.findByMinisterioIdIn(ministerioIds).stream()
                .map(e -> EscalaResponseDTO.fromEntity(e, e.getDatas().size(), e.getConfirmacoes().size()))
                .toList();
    }

    /** Escalas de um ministério específico (membro do ministério ou pastor). */
    @Transactional(readOnly = true)
    public List<EscalaResponseDTO> listarEscalasDoMinisterio(Long ministerioId, Usuario usuario) {
        validarMembro(usuario, ministerioId);
        return escalaRepository.findByMinisterioId(ministerioId).stream()
                .map(e -> EscalaResponseDTO.fromEntity(e, e.getDatas().size(), e.getConfirmacoes().size()))
                .toList();
    }

    /** Detalhe de uma escala (requer vínculo com o ministério ou pastor). */
    @Transactional(readOnly = true)
    public EscalaDetalhadaResponseDTO buscarEscala(Long escalaId, Usuario usuario) {
        Escala escala = escalaRepository.findById(escalaId)
                .orElseThrow(() -> new ResourceNotFoundException("Escala", escalaId));
        validarMembro(usuario, escala.getMinisterioId());
        return escalaService.buscarPorId(escalaId);
    }

    /** Cria escala vinculada a um ministério (apenas líder/pastor). */
    @Transactional
    public EscalaResponseDTO criarEscala(Long ministerioId, String titulo, Usuario usuario) {
        validarLider(usuario, ministerioId);
        return escalaService.criar(new EscalaRequestDTO(titulo, ministerioId));
    }

    // ===== MONTAGEM DA ESCALA (líder do ministério) =====

    /** Adiciona data à escala (líder do ministério/pastor). */
    @Transactional
    public EscalaDataDTO adicionarData(Long escalaId, EscalaDataDTO dto, Usuario usuario) {
        validarLiderDaEscala(usuario, escalaId);
        return escalaService.adicionarData(escalaId, dto);
    }

    /** Remove data da escala (líder do ministério/pastor). */
    @Transactional
    public void removerData(Long escalaId, Long dataId, Usuario usuario) {
        validarLiderDaEscala(usuario, escalaId);
        escalaService.removerData(escalaId, dataId);
    }

    /** Salva designações (montagem) de uma data (líder do ministério/pastor). */
    @Transactional
    public void salvarDesignacoes(Long escalaId, Long dataId, List<EscalaDesignacaoRequestDTO> designacoes, Usuario usuario) {
        validarLiderDaEscala(usuario, escalaId);
        escalaService.salvarDesignacoes(escalaId, dataId, designacoes);
    }

    /** Salva músicas (repertório) de uma data (líder do ministério/pastor). */
    @Transactional
    public void salvarMusicas(Long escalaId, Long dataId, List<EscalaMusicaDTO> musicas, Usuario usuario) {
        validarLiderDaEscala(usuario, escalaId);
        escalaService.salvarMusicas(escalaId, dataId, musicas);
    }

    /** Abre/fecha a escala para confirmações (líder do ministério/pastor). */
    @Transactional
    public void toggleEscala(Long escalaId, Usuario usuario) {
        validarLiderDaEscala(usuario, escalaId);
        escalaService.toggleEscala(escalaId);
    }

    /** Gera/obtém o link público do resultado (líder do ministério/pastor). */
    @Transactional
    public String gerarLinkResultado(Long escalaId, Usuario usuario) {
        validarLiderDaEscala(usuario, escalaId);
        return escalaService.gerarLinkResultado(escalaId);
    }

    // ===== CONFIRMAÇÃO LOGADA (sem token) =====

    /** Confirma ou atualiza a disponibilidade do membro logado para as datas escolhidas. */
    @Transactional
    public EscalaConfirmacaoResponseDTO confirmarDisponibilidade(Long escalaId, List<Long> dataIds, Usuario usuario) {
        Escala escala = escalaRepository.findById(escalaId)
                .orElseThrow(() -> new ResourceNotFoundException("Escala", escalaId));
        validarMembro(usuario, escala.getMinisterioId());

        Membro membro = membroDoUsuario(usuario)
                .orElseThrow(() -> new BusinessException("Seu usuário não está vinculado a um membro da igreja"));

        EscalaConfirmacao confirmacao = confirmacaoRepository.findByEscalaIdAndMembroId(escalaId, membro.getId())
                .orElse(EscalaConfirmacao.builder()
                        .escala(escala)
                        .membro(membro)
                        .nome(membro.getNome())
                        .celular(membro.getTelefone())
                        .build());

        var datas = escalaDataRepository.findAllById(dataIds);
        // Garante que todas as datas confirmadas pertencem a esta escala
        if (datas.size() != dataIds.size()
                || datas.stream().anyMatch(d -> !d.getEscala().getId().equals(escala.getId()))) {
            throw new BusinessException("Uma ou mais datas não pertencem a esta escala");
        }
        confirmacao.setDatas(datas);
        confirmacao = confirmacaoRepository.save(confirmacao);

        return EscalaConfirmacaoResponseDTO.fromEntity(confirmacao, dataIds);
    }

    /** Cancela a confirmação do membro logado em uma escala. */
    @Transactional
    public void cancelarConfirmacao(Long escalaId, Usuario usuario) {
        Membro membro = membroDoUsuario(usuario)
                .orElseThrow(() -> new BusinessException("Seu usuário não está vinculado a um membro da igreja"));
        confirmacaoRepository.findByEscalaIdAndMembroId(escalaId, membro.getId())
                .ifPresent(confirmacaoRepository::delete);
    }

    // ===== HELPERS / PERMISSÕES =====

    /** Pastores têm acesso ampliado a todos os ministérios. */
    private boolean isAcessoAmpliado(Usuario usuario) {
        return usuario.getNivel() == NivelAcesso.PASTOR_PRESIDENTE
                || usuario.getNivel() == NivelAcesso.PASTOR_AUXILIAR;
    }

    /**
     * Membro da igreja vinculado ao usuário (via idPessoa), independente do nível de acesso.
     * Um pastor/tesoureiro/secretário que também serve em um ministério mantém seus vínculos
     * (papéis compostos) e pode confirmar disponibilidade normalmente.
     */
    private Optional<Membro> membroDoUsuario(Usuario usuario) {
        if (usuario.getIdPessoa() == null) {
            return Optional.empty();
        }
        return membroRepository.findById(usuario.getIdPessoa());
    }

    /**
     * Mapa ministerioId → papel do usuário logado (vazio se o usuário não possui idPessoa).
     * Usado para exibir o papel real do pastor nos ministérios onde ele também serve.
     */
    private Map<Long, PapelMinisterio> papeisDoUsuario(Usuario usuario) {
        Long membroId = membroDoUsuario(usuario).map(Membro::getId).orElse(null);
        if (membroId == null) return Map.of();
        return ministerioMembroRepository.findByMembroId(membroId).stream()
                .collect(Collectors.toMap(v -> v.getMinisterio().getId(), MinisterioMembro::getPapel));
    }

    /** Lança exceção se o usuário não for líder do ministério (nem pastor). */
    private void validarLider(Usuario usuario, Long ministerioId) {
        if (isAcessoAmpliado(usuario)) return;
        Long membroId = membroDoUsuario(usuario).map(Membro::getId).orElse(null);
        if (membroId == null
                || !ministerioMembroRepository.existsByMinisterioIdAndMembroIdAndPapel(
                        ministerioId, membroId, PapelMinisterio.LIDER)) {
            throw new BusinessException("Acesso negado: apenas o líder deste ministério pode realizar esta ação");
        }
    }

    /** Valida liderança sobre a escala (pelo ministério ao qual ela pertence). */
    private void validarLiderDaEscala(Usuario usuario, Long escalaId) {
        Escala escala = escalaRepository.findById(escalaId)
                .orElseThrow(() -> new ResourceNotFoundException("Escala", escalaId));
        if (escala.getMinisterioId() == null) {
            // Escala genérica: apenas pastores podem montar
            if (!isAcessoAmpliado(usuario)) {
                throw new BusinessException("Acesso negado: esta escala não pertence a um ministério");
            }
            return;
        }
        validarLider(usuario, escala.getMinisterioId());
    }

    /** Lança exceção se o usuário não for membro do ministério (nem pastor). */
    private void validarMembro(Usuario usuario, Long ministerioId) {
        if (isAcessoAmpliado(usuario)) return;
        Long membroId = membroDoUsuario(usuario).map(Membro::getId).orElse(null);
        if (membroId == null) {
            throw new BusinessException("Acesso negado: usuário não é membro da igreja");
        }
        if (ministerioId != null && !ministerioMembroRepository.existsByMinisterioIdAndMembroId(ministerioId, membroId)) {
            throw new BusinessException("Acesso negado: você não participa deste ministério");
        }
    }
}
