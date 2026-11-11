package com.sg.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/config")
@Tag(name = "Configurações", description = "Configurações do sistema")
public class ConfiguracaoController {

    private final ConfiguracaoService configuracaoService;

    public ConfiguracaoController(ConfiguracaoService configuracaoService) {
        this.configuracaoService = configuracaoService;
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO','SECRETARIO')")
    @GetMapping
    @Operation(summary = "Listar todas as configurações")
    public ResponseEntity<List<Configuracao>> listar() {
        return ResponseEntity.ok(configuracaoService.listarTodas());
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO','SECRETARIO')")
    @GetMapping("/{idOuNome}")
    @Operation(summary = "Buscar configuração por ID (numérico) ou por nome")
    public ResponseEntity<Configuracao> buscar(@PathVariable String idOuNome) {
        return ResponseEntity.ok(configuracaoService.buscarPorIdOuNome(idOuNome));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR')")
    @PostMapping
    @Operation(summary = "Criar/atualizar configuração")
    public ResponseEntity<Configuracao> salvar(@RequestBody Configuracao configuracao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.salvar(configuracao));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR')")
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar configuração")
    public ResponseEntity<Configuracao> atualizar(@PathVariable Long id, @RequestBody Configuracao configuracao) {
        configuracao.setId(id);
        return ResponseEntity.ok(configuracaoService.salvar(configuracao));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar configuração")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
