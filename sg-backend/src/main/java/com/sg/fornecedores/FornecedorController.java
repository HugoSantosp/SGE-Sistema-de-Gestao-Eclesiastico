package com.sg.fornecedores;

import com.sg.fornecedores.dto.FornecedorRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/fornecedores")
@Tag(name = "Fornecedores", description = "CRUD de fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @GetMapping
    @Operation(summary = "Listar fornecedores")
    public ResponseEntity<List<Fornecedor>> listar() {
        return ResponseEntity.ok(fornecedorService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar fornecedor por ID")
    public ResponseEntity<Fornecedor> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(fornecedorService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo fornecedor")
    public ResponseEntity<Fornecedor> criar(@RequestBody @Valid FornecedorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fornecedorService.salvar(toEntity(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar fornecedor")
    public ResponseEntity<Fornecedor> atualizar(@PathVariable Long id, @RequestBody @Valid FornecedorRequestDTO dto) {
        return ResponseEntity.ok(fornecedorService.atualizar(id, toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar fornecedor")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        fornecedorService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private Fornecedor toEntity(FornecedorRequestDTO dto) {
        return Fornecedor.builder()
                .nome(dto.nome()).telefone(dto.telefone()).endereco(dto.endereco())
                .email(dto.email()).produto(dto.produto()).build();
    }
}
