package com.sg.tesoureiros;

import com.sg.tesoureiros.dto.TesoureiroRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tesoureiros")
@Tag(name = "Tesoureiros", description = "CRUD de tesoureiros")
public class TesoureiroController {

    private final TesoureiroService tesoureiroService;

    public TesoureiroController(TesoureiroService tesoureiroService) {
        this.tesoureiroService = tesoureiroService;
    }

    @GetMapping
    @Operation(summary = "Listar tesoureiros")
    public ResponseEntity<List<Tesoureiro>> listar() {
        return ResponseEntity.ok(tesoureiroService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tesoureiro por ID")
    public ResponseEntity<Tesoureiro> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tesoureiroService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo tesoureiro")
    public ResponseEntity<Tesoureiro> criar(@RequestBody @Valid TesoureiroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tesoureiroService.salvar(toEntity(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tesoureiro")
    public ResponseEntity<Tesoureiro> atualizar(@PathVariable Long id, @RequestBody @Valid TesoureiroRequestDTO dto) {
        return ResponseEntity.ok(tesoureiroService.atualizar(id, toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar tesoureiro")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        tesoureiroService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private Tesoureiro toEntity(TesoureiroRequestDTO dto) {
        return Tesoureiro.builder()
                .nome(dto.nome()).email(dto.email()).documento(dto.documento())
                .telefone(dto.telefone()).endereco(dto.endereco()).foto(dto.foto())
                .dataNasc(dto.dataNasc()).build();
    }
}
