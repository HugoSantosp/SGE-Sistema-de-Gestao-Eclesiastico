package com.sg.meuministerio;

import com.sg.escala.dto.EscalaConfirmacaoResponseDTO;
import com.sg.escala.dto.EscalaDataDTO;
import com.sg.escala.dto.EscalaDesignacaoRequestDTO;
import com.sg.escala.dto.EscalaDetalhadaResponseDTO;
import com.sg.escala.dto.EscalaMusicaDTO;
import com.sg.escala.dto.EscalaResponseDTO;
import com.sg.meuministerio.dto.AdicionarMembroRequestDTO;
import com.sg.meuministerio.dto.AlterarPapelRequestDTO;
import com.sg.meuministerio.dto.ConfirmarEscalaRequestDTO;
import com.sg.meuministerio.dto.CriarEscalaRequestDTO;
import com.sg.meuministerio.dto.MembroMinisterioDTO;
import com.sg.meuministerio.dto.MinisterioDoUsuarioDTO;
import com.sg.shared.exceptions.BusinessException;
import com.sg.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API do app "MeuMinisterio" (estilo Voluts) — icertag.com.br/SGE-MeuMinisterio.
 * <p>
 * Endpoints para o membro confirmar disponibilidade e para o líder
 * gerenciar membros e escalas do seu ministério.
 */
@RestController
@RequestMapping("/api/meu-ministerio")
@Tag(name = "MeuMinisterio (app)", description = "Endpoints do app MeuMinisterio — escalas por ministério, disponibilidade e gestão de membros")
public class MeuMinisterioController {

    private final MeuMinisterioService meuMinisterioService;

    public MeuMinisterioController(MeuMinisterioService meuMinisterioService) {
        this.meuMinisterioService = meuMinisterioService;
    }

    private Usuario usuarioLogado(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new BusinessException("Usuário não autenticado");
        }
        return usuario;
    }

    // ===== MINISTÉRIOS =====

    @GetMapping("/ministerios")
    @Operation(summary = "Ministérios do usuário (todos para pastor; vínculos para membro)")
    public ResponseEntity<List<MinisterioDoUsuarioDTO>> meusMinisterios(Authentication authentication) {
        return ResponseEntity.ok(meuMinisterioService.listarMeusMinisterios(usuarioLogado(authentication)));
    }

    @GetMapping("/ministerios/liderados")
    @Operation(summary = "Ministérios onde o usuário é líder")
    public ResponseEntity<List<MinisterioDoUsuarioDTO>> ministeriosLiderados(Authentication authentication) {
        return ResponseEntity.ok(meuMinisterioService.listarMinisteriosLiderados(usuarioLogado(authentication)));
    }

    // ===== MEMBROS DO MINISTÉRIO (líder) =====

    @GetMapping("/ministerios/{ministerioId}/membros")
    @Operation(summary = "Membros do ministério (líder/pastor)")
    public ResponseEntity<List<MembroMinisterioDTO>> membrosDoMinisterio(
            @PathVariable Long ministerioId,
            Authentication authentication) {
        return ResponseEntity.ok(meuMinisterioService.listarMembrosDoMinisterio(ministerioId, usuarioLogado(authentication)));
    }

    @PostMapping("/ministerios/{ministerioId}/membros")
    @Operation(summary = "Adicionar membro ao ministério (líder/pastor)")
    public ResponseEntity<Void> adicionarMembro(
            @PathVariable Long ministerioId,
            @RequestBody @Valid AdicionarMembroRequestDTO dto,
            Authentication authentication) {
        meuMinisterioService.adicionarMembro(ministerioId, dto, usuarioLogado(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/ministerios/{ministerioId}/membros/{membroId}")
    @Operation(summary = "Alterar papel do membro no ministério (líder/pastor)")
    public ResponseEntity<Void> alterarPapel(
            @PathVariable Long ministerioId,
            @PathVariable Long membroId,
            @RequestBody @Valid AlterarPapelRequestDTO dto,
            Authentication authentication) {
        meuMinisterioService.alterarPapel(ministerioId, membroId, dto.papel(), usuarioLogado(authentication));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/ministerios/{ministerioId}/membros/{membroId}")
    @Operation(summary = "Remover membro do ministério (líder/pastor)")
    public ResponseEntity<Void> removerMembro(
            @PathVariable Long ministerioId,
            @PathVariable Long membroId,
            Authentication authentication) {
        meuMinisterioService.removerMembro(ministerioId, membroId, usuarioLogado(authentication));
        return ResponseEntity.noContent().build();
    }

    // ===== ESCALAS =====

    @GetMapping("/escalas")
    @Operation(summary = "Escalas dos ministérios do usuário")
    public ResponseEntity<List<EscalaResponseDTO>> minhasEscalas(Authentication authentication) {
        return ResponseEntity.ok(meuMinisterioService.listarMinhasEscalas(usuarioLogado(authentication)));
    }

    @GetMapping("/ministerios/{ministerioId}/escalas")
    @Operation(summary = "Escalas de um ministério (membro do ministério/pastor)")
    public ResponseEntity<List<EscalaResponseDTO>> escalasDoMinisterio(
            @PathVariable Long ministerioId,
            Authentication authentication) {
        return ResponseEntity.ok(meuMinisterioService.listarEscalasDoMinisterio(ministerioId, usuarioLogado(authentication)));
    }

    @PostMapping("/ministerios/{ministerioId}/escalas")
    @Operation(summary = "Criar escala no ministério (líder/pastor)")
    public ResponseEntity<EscalaResponseDTO> criarEscala(
            @PathVariable Long ministerioId,
            @RequestBody @Valid CriarEscalaRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meuMinisterioService.criarEscala(ministerioId, dto.titulo(), usuarioLogado(authentication)));
    }

    @GetMapping("/escalas/{escalaId}")
    @Operation(summary = "Detalhe da escala (membro do ministério/pastor)")
    public ResponseEntity<EscalaDetalhadaResponseDTO> buscarEscala(
            @PathVariable Long escalaId,
            Authentication authentication) {
        return ResponseEntity.ok(meuMinisterioService.buscarEscala(escalaId, usuarioLogado(authentication)));
    }

    // ===== MONTAGEM DA ESCALA (líder/pastor) =====

    @PostMapping("/escalas/{escalaId}/datas")
    @Operation(summary = "Adicionar data à escala (líder/pastor)")
    public ResponseEntity<EscalaDataDTO> adicionarData(
            @PathVariable Long escalaId,
            @RequestBody @Valid EscalaDataDTO dto,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meuMinisterioService.adicionarData(escalaId, dto, usuarioLogado(authentication)));
    }

    @DeleteMapping("/escalas/{escalaId}/datas/{dataId}")
    @Operation(summary = "Remover data da escala (líder/pastor)")
    public ResponseEntity<Void> removerData(
            @PathVariable Long escalaId,
            @PathVariable Long dataId,
            Authentication authentication) {
        meuMinisterioService.removerData(escalaId, dataId, usuarioLogado(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/escalas/{escalaId}/datas/{dataId}/designacoes")
    @Operation(summary = "Salvar designações (montagem) de uma data (líder/pastor)")
    public ResponseEntity<Void> salvarDesignacoes(
            @PathVariable Long escalaId,
            @PathVariable Long dataId,
            @RequestBody @Valid List<EscalaDesignacaoRequestDTO> designacoes,
            Authentication authentication) {
        meuMinisterioService.salvarDesignacoes(escalaId, dataId, designacoes, usuarioLogado(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/escalas/{escalaId}/datas/{dataId}/musicas")
    @Operation(summary = "Salvar músicas (repertório) de uma data (líder/pastor)")
    public ResponseEntity<Void> salvarMusicas(
            @PathVariable Long escalaId,
            @PathVariable Long dataId,
            @RequestBody @Valid List<EscalaMusicaDTO> musicas,
            Authentication authentication) {
        meuMinisterioService.salvarMusicas(escalaId, dataId, musicas, usuarioLogado(authentication));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/escalas/{escalaId}/toggle")
    @Operation(summary = "Abrir/fechar escala para confirmações (líder/pastor)")
    public ResponseEntity<Void> toggleEscala(
            @PathVariable Long escalaId,
            Authentication authentication) {
        meuMinisterioService.toggleEscala(escalaId, usuarioLogado(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/escalas/{escalaId}/gerar-link")
    @Operation(summary = "Gerar/obter link público do resultado (líder/pastor)")
    public ResponseEntity<Map<String, String>> gerarLinkResultado(
            @PathVariable Long escalaId,
            Authentication authentication) {
        String token = meuMinisterioService.gerarLinkResultado(escalaId, usuarioLogado(authentication));
        return ResponseEntity.ok(Map.of("resultadoToken", token));
    }

    // ===== CONFIRMAÇÃO DE DISPONIBILIDADE (logada) =====

    @PostMapping("/escalas/{escalaId}/confirmar")
    @Operation(summary = "Confirmar/atualizar disponibilidade do membro logado")
    public ResponseEntity<EscalaConfirmacaoResponseDTO> confirmarDisponibilidade(
            @PathVariable Long escalaId,
            @RequestBody @Valid ConfirmarEscalaRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(meuMinisterioService.confirmarDisponibilidade(
                escalaId, dto.dataIds(), usuarioLogado(authentication)));
    }

    @DeleteMapping("/escalas/{escalaId}/confirmar")
    @Operation(summary = "Cancelar confirmação do membro logado")
    public ResponseEntity<Void> cancelarConfirmacao(
            @PathVariable Long escalaId,
            Authentication authentication) {
        meuMinisterioService.cancelarConfirmacao(escalaId, usuarioLogado(authentication));
        return ResponseEntity.noContent().build();
    }
}
