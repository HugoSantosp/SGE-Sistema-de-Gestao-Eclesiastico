package com.sg.igrejas;

import com.sg.igrejas.dto.IgrejaRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/igrejas")
@Tag(name = "Igrejas", description = "CRUD de igrejas (matriz e filiais)")
public class IgrejaController {

    private final IgrejaService igrejaService;

    public IgrejaController(IgrejaService igrejaService) {
        this.igrejaService = igrejaService;
    }

    @GetMapping
    @Operation(summary = "Listar igrejas")
    public ResponseEntity<List<Igreja>> listar() {
        return ResponseEntity.ok(igrejaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar igreja por ID")
    public ResponseEntity<Igreja> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(igrejaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar nova igreja")
    public ResponseEntity<Igreja> criar(@RequestBody @Valid IgrejaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(igrejaService.salvar(toEntity(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar igreja")
    public ResponseEntity<Igreja> atualizar(@PathVariable Long id, @RequestBody @Valid IgrejaRequestDTO dto) {
        return ResponseEntity.ok(igrejaService.atualizar(id, toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar igreja")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        igrejaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private Igreja toEntity(IgrejaRequestDTO dto) {
        return Igreja.builder()
                .nome(dto.nome()).telefone(dto.telefone()).endereco(dto.endereco())
                .obs(dto.obs()).foto(dto.foto()).matriz(dto.matriz())
                .pastorId(dto.pastorId()).build();
    }
}
