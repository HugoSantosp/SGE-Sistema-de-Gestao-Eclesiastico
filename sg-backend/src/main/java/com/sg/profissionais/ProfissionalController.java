package com.sg.profissionais;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profissionais")
@Tag(name = "Profissionais", description = "CRUD do mural de profissionais da igreja")
public class ProfissionalController {
    private final ProfissionalService profissionalService;

    public ProfissionalController(ProfissionalService profissionalService) {
        this.profissionalService = profissionalService;
    }

    @GetMapping
    public ResponseEntity<List<Profissional>> listar() {
        return ResponseEntity.ok(profissionalService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profissional> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(profissionalService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Profissional> criar(@RequestBody Profissional profissional) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profissionalService.salvar(profissional));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profissional> atualizar(@PathVariable Long id, @RequestBody Profissional profissional) {
        profissional.setId(id);
        return ResponseEntity.ok(profissionalService.salvar(profissional));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        profissionalService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
