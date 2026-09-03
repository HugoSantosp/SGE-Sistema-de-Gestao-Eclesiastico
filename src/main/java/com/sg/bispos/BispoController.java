package com.sg.bispos;

import com.sg.bispos.dto.BispoRequestDTO;
import com.sg.bispos.dto.BispoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bispos")
@Tag(name = "Bispos", description = "CRUD de bispos / pastor presidente")
public class BispoController {

    private final BispoService bispoService;

    public BispoController(BispoService bispoService) {
        this.bispoService = bispoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os bispos")
    public ResponseEntity<List<Bispo>> listar() {
        return ResponseEntity.ok(bispoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar bispo por ID")
    public ResponseEntity<Bispo> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(bispoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo bispo")
    public ResponseEntity<Bispo> criar(@RequestBody @Valid BispoRequestDTO dto) {
        Bispo bispo = toEntity(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(bispoService.salvar(bispo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar bispo")
    public ResponseEntity<Bispo> atualizar(@PathVariable Long id, @RequestBody @Valid BispoRequestDTO dto) {
        Bispo bispo = toEntity(dto);
        return ResponseEntity.ok(bispoService.atualizar(id, bispo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar bispo")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        bispoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private Bispo toEntity(BispoRequestDTO dto) {
        return Bispo.builder()
                .nome(dto.nome())
                .email(dto.email())
                .documento(dto.documento())
                .telefone(dto.telefone())
                .endereco(dto.endereco())
                .foto(dto.foto())
                .dataNasc(dto.dataNasc())
                .build();
    }
}
