package com.sg.escala;

import com.sg.escala.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/escalas")
@Tag(name = "Escalas de Louvor", description = "Gerenciamento de escalas de louvor (admin)")
public class EscalaController {

    private final EscalaService escalaService;

    public EscalaController(EscalaService escalaService) {
        this.escalaService = escalaService;
    }

    // ===== ESCALAS =====

    @GetMapping
    @Operation(summary = "Listar todas as escalas")
    public ResponseEntity<List<EscalaResponseDTO>> listar() {
        return ResponseEntity.ok(escalaService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar escala por ID (completa: datas + confirmações + designações + músicas)")
    public ResponseEntity<EscalaDetalhadaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(escalaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar nova escala")
    public ResponseEntity<EscalaResponseDTO> criar(@RequestBody @Valid EscalaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(escalaService.criar(dto));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Abrir/fechar escala para confirmações")
    public ResponseEntity<Void> toggleEscala(@PathVariable Long id) {
        escalaService.toggleEscala(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar escala")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        escalaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/gerar-link")
    @Operation(summary = "Gerar (ou obter) link público do resultado da escala")
    public ResponseEntity<Map<String, String>> gerarLinkResultado(@PathVariable Long id) {
        String token = escalaService.gerarLinkResultado(id);
        return ResponseEntity.ok(Map.of("resultadoToken", token));
    }

    // ===== DATAS =====

    @PostMapping("/{escalaId}/datas")
    @Operation(summary = "Adicionar data a uma escala")
    public ResponseEntity<EscalaDataDTO> adicionarData(
            @PathVariable Long escalaId,
            @RequestBody @Valid EscalaDataDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(escalaService.adicionarData(escalaId, dto));
    }

    @DeleteMapping("/{escalaId}/datas/{dataId}")
    @Operation(summary = "Remover data de uma escala")
    public ResponseEntity<Void> removerData(
            @PathVariable Long escalaId,
            @PathVariable Long dataId) {
        escalaService.removerData(escalaId, dataId);
        return ResponseEntity.noContent().build();
    }

    // ===== DESIGNAÇÕES (montagem) =====

    @GetMapping("/{escalaId}/datas/{dataId}/designacoes")
    @Operation(summary = "Listar designações de uma data")
    public ResponseEntity<List<EscalaDesignacaoResponseDTO>> listarDesignacoes(
            @PathVariable Long escalaId,
            @PathVariable Long dataId) {
        return ResponseEntity.ok(escalaService.listarDesignacoes(dataId));
    }

    @PostMapping("/{escalaId}/datas/{dataId}/designacoes")
    @Operation(summary = "Salvar designações (montagem) para uma data")
    public ResponseEntity<Void> salvarDesignacoes(
            @PathVariable Long escalaId,
            @PathVariable Long dataId,
            @RequestBody @Valid List<EscalaDesignacaoRequestDTO> designacoes) {
        escalaService.salvarDesignacoes(escalaId, dataId, designacoes);
        return ResponseEntity.noContent().build();
    }

    // ===== MÚSICAS (repertório) =====

    @GetMapping("/{escalaId}/datas/{dataId}/musicas")
    @Operation(summary = "Listar músicas de uma data")
    public ResponseEntity<List<EscalaMusicaDTO>> listarMusicas(
            @PathVariable Long escalaId,
            @PathVariable Long dataId) {
        return ResponseEntity.ok(escalaService.listarMusicas(dataId));
    }

    @PostMapping("/{escalaId}/datas/{dataId}/musicas")
    @Operation(summary = "Salvar músicas (repertório) para uma data")
    public ResponseEntity<Void> salvarMusicas(
            @PathVariable Long escalaId,
            @PathVariable Long dataId,
            @RequestBody @Valid List<EscalaMusicaDTO> musicas) {
        escalaService.salvarMusicas(escalaId, dataId, musicas);
        return ResponseEntity.noContent().build();
    }

    // ===== INSTRUMENTOS =====

    @GetMapping("/instrumentos")
    @Operation(summary = "Listar instrumentos disponíveis para designação")
    public ResponseEntity<List<String>> listarInstrumentos() {
        return ResponseEntity.ok(EscalaService.INSTRUMENTOS);
    }
}
