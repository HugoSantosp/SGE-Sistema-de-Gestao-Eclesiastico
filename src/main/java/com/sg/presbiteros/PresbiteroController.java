package com.sg.presbiteros;

import com.sg.presbiteros.dto.PresbiteroRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/presbiteros")
@Tag(name = "Presbíteros", description = "CRUD de presbíteros / pastores auxiliares")
public class PresbiteroController {

    private final PresbiteroService presbiteroService;

    public PresbiteroController(PresbiteroService presbiteroService) {
        this.presbiteroService = presbiteroService;
    }

    @GetMapping
    @Operation(summary = "Listar presbíteros")
    public ResponseEntity<List<Presbitero>> listar() {
        return ResponseEntity.ok(presbiteroService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar presbítero por ID")
    public ResponseEntity<Presbitero> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(presbiteroService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo presbítero")
    public ResponseEntity<Presbitero> criar(@RequestBody @Valid PresbiteroRequestDTO dto) {
        Presbitero presbitero = toEntity(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(presbiteroService.salvar(presbitero));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar presbítero")
    public ResponseEntity<Presbitero> atualizar(@PathVariable Long id, @RequestBody @Valid PresbiteroRequestDTO dto) {
        return ResponseEntity.ok(presbiteroService.atualizar(id, toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar presbítero")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        presbiteroService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private Presbitero toEntity(PresbiteroRequestDTO dto) {
        return Presbitero.builder()
                .nome(dto.nome()).email(dto.email()).documento(dto.documento())
                .telefone(dto.telefone()).endereco(dto.endereco()).foto(dto.foto())
                .dataNasc(dto.dataNasc()).obs(dto.obs()).build();
    }
}
