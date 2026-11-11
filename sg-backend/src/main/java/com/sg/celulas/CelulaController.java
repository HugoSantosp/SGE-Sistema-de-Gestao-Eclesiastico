package com.sg.celulas;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/celulas")
@Tag(name = "Células", description = "CRUD de células/grupos pequenos")
public class CelulaController {
    private final CelulaService celulaService;

    public CelulaController(CelulaService celulaService) {
        this.celulaService = celulaService;
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','SECRETARIO')")
    @GetMapping
    public ResponseEntity<List<Celula>> listar() {
        return ResponseEntity.ok(celulaService.listarTodos());
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','SECRETARIO')")
    @GetMapping("/{id}")
    public ResponseEntity<Celula> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(celulaService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','SECRETARIO')")
    @PostMapping
    public ResponseEntity<Celula> criar(@RequestBody Celula celula) {
        return ResponseEntity.status(HttpStatus.CREATED).body(celulaService.salvar(celula));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','SECRETARIO')")
    @PutMapping("/{id}")
    public ResponseEntity<Celula> atualizar(@PathVariable Long id, @RequestBody Celula celula) {
        celula.setId(id);
        return ResponseEntity.ok(celulaService.salvar(celula));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','SECRETARIO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        celulaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
