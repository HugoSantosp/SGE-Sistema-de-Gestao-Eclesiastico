package com.sg.ministerios;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ministerios")
@Tag(name = "Ministérios", description = "CRUD de ministérios da igreja")
public class MinisterioController {

    private final MinisterioService ministerioService;

    public MinisterioController(MinisterioService ministerioService) {
        this.ministerioService = ministerioService;
    }

    @GetMapping
    @Operation(summary = "Listar ministérios")
    public ResponseEntity<List<Ministerio>> listar() {
        return ResponseEntity.ok(ministerioService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ministério por ID")
    public ResponseEntity<Ministerio> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ministerioService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo ministério")
    public ResponseEntity<Ministerio> criar(@RequestBody Ministerio ministerio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ministerioService.salvar(ministerio));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ministério")
    public ResponseEntity<Ministerio> atualizar(@PathVariable Long id, @RequestBody Ministerio ministerio) {
        ministerio.setId(id);
        return ResponseEntity.ok(ministerioService.salvar(ministerio));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar ministério")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        ministerioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
