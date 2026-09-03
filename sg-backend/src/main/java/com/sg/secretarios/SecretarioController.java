package com.sg.secretarios;

import com.sg.secretarios.dto.SecretarioRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/secretarios")
@Tag(name = "Secretários", description = "CRUD de secretários")
public class SecretarioController {

    private final SecretarioService secretarioService;

    public SecretarioController(SecretarioService secretarioService) {
        this.secretarioService = secretarioService;
    }

    @GetMapping
    @Operation(summary = "Listar secretários")
    public ResponseEntity<List<Secretario>> listar() {
        return ResponseEntity.ok(secretarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar secretário por ID")
    public ResponseEntity<Secretario> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(secretarioService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo secretário")
    public ResponseEntity<Secretario> criar(@RequestBody @Valid SecretarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(secretarioService.salvar(toEntity(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar secretário")
    public ResponseEntity<Secretario> atualizar(@PathVariable Long id, @RequestBody @Valid SecretarioRequestDTO dto) {
        return ResponseEntity.ok(secretarioService.atualizar(id, toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar secretário")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        secretarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private Secretario toEntity(SecretarioRequestDTO dto) {
        return Secretario.builder()
                .nome(dto.nome()).email(dto.email()).documento(dto.documento())
                .telefone(dto.telefone()).endereco(dto.endereco()).foto(dto.foto())
                .dataNasc(dto.dataNasc()).build();
    }
}
