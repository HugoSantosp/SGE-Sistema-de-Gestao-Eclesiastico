package com.sg.escala;

import com.sg.escala.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publico/escala")
@Tag(name = "Escala de Louvor (Público)", description = "Endpoints públicos acessados via token (sem autenticação)")
public class EscalaPublicaController {

    private final EscalaService escalaService;

    public EscalaPublicaController(EscalaService escalaService) {
        this.escalaService = escalaService;
    }

    // ===== CONFIRMAÇÃO DE DISPONIBILIDADE =====

    @GetMapping("/{token}")
    @Operation(summary = "Visualizar escala pública com datas disponíveis para confirmação")
    public ResponseEntity<EscalaDetalhadaResponseDTO> visualizarEscala(@PathVariable String token) {
        return ResponseEntity.ok(escalaService.buscarPorTokenPublico(token));
    }

    @PostMapping("/{token}/confirmar")
    @Operation(summary = "Confirmar disponibilidade para datas selecionadas")
    public ResponseEntity<EscalaConfirmacaoResponseDTO> confirmar(
            @PathVariable String token,
            @RequestBody @Valid EscalaConfirmacaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(escalaService.confirmarDisponibilidade(token, dto));
    }

    // ===== RESULTADO (escala montada) =====

    @GetMapping("/resultado/{token}")
    @Operation(summary = "Visualizar resultado da escala montada")
    public ResponseEntity<EscalaDetalhadaResponseDTO> visualizarResultado(@PathVariable String token) {
        return ResponseEntity.ok(escalaService.buscarResultadoPorToken(token));
    }

    // ===== REPERTÓRIO DO MINISTRO =====

    @GetMapping("/resultado/{token}/ministro")
    @Operation(summary = "Visualizar datas futuras para edição de repertório (ministro)")
    public ResponseEntity<EscalaDetalhadaResponseDTO> ministroRepertorio(@PathVariable String token) {
        return ResponseEntity.ok(escalaService.buscarResultadoPorToken(token));
    }

    @PostMapping("/resultado/{token}/ministro/{dateId}")
    @Operation(summary = "Salvar músicas do repertório (ministro) usando token do resultado")
    public ResponseEntity<Void> salvarMusicas(
            @PathVariable String token,
            @PathVariable Long dateId,
            @RequestBody @Valid List<EscalaMusicaDTO> musicas) {
        escalaService.salvarMusicasPorToken(token, dateId, musicas);
        return ResponseEntity.noContent().build();
    }
}
