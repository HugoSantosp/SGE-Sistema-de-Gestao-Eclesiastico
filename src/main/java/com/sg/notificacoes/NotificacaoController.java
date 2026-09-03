package com.sg.notificacoes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificações", description = "Gerenciamento de notificações do sistema")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    @Operation(summary = "Listar todas as notificações")
    public ResponseEntity<List<Notificacao>> listar() {
        return ResponseEntity.ok(notificacaoService.listarTodas());
    }

    @GetMapping("/hoje")
    @Operation(summary = "Listar notificações do dia")
    public ResponseEntity<List<Notificacao>> listarDoDia() {
        return ResponseEntity.ok(notificacaoService.listarDoDia());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar notificação por ID")
    public ResponseEntity<Notificacao> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(notificacaoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar nova notificação")
    public ResponseEntity<Notificacao> criar(@RequestBody @Valid Notificacao notificacao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacaoService.salvar(notificacao));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar notificação")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        notificacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
