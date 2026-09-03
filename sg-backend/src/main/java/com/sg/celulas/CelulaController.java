package com.sg.celulas;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<Celula>> listar() {
        return ResponseEntity.ok(celulaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Celula> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(celulaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Celula> criar(@RequestBody Celula celula) {
        return ResponseEntity.status(HttpStatus.CREATED).body(celulaService.salvar(celula));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Celula> atualizar(@PathVariable Long id, @RequestBody Celula celula) {
        celula.setId(id);
        return ResponseEntity.ok(celulaService.salvar(celula));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        celulaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
