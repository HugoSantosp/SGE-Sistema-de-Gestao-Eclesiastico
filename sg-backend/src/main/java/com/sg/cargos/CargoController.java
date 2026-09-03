package com.sg.cargos;

import com.sg.cargos.dto.CargoRequestDTO;
import com.sg.cargos.dto.CargoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cargos")
@Tag(name = "Cargos", description = "CRUD de cargos eclesiásticos")
public class CargoController {

    private final CargoService cargoService;

    public CargoController(CargoService cargoService) {
        this.cargoService = cargoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os cargos")
    public ResponseEntity<List<CargoResponseDTO>> listar() {
        List<CargoResponseDTO> dtos = cargoService.listarTodos().stream()
                .map(c -> new CargoResponseDTO(c.getId(), c.getNome()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cargo por ID")
    public ResponseEntity<CargoResponseDTO> buscarPorId(@PathVariable Long id) {
        Cargo cargo = cargoService.buscarPorId(id);
        return ResponseEntity.ok(new CargoResponseDTO(cargo.getId(), cargo.getNome()));
    }

    @PostMapping
    @Operation(summary = "Criar novo cargo")
    public ResponseEntity<CargoResponseDTO> criar(@RequestBody @Valid CargoRequestDTO dto) {
        Cargo cargo = Cargo.builder().nome(dto.nome()).build();
        cargo = cargoService.salvar(cargo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CargoResponseDTO(cargo.getId(), cargo.getNome()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cargo")
    public ResponseEntity<CargoResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid CargoRequestDTO dto) {
        Cargo cargo = Cargo.builder().nome(dto.nome()).build();
        cargo = cargoService.atualizar(id, cargo);
        return ResponseEntity.ok(new CargoResponseDTO(cargo.getId(), cargo.getNome()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar cargo")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        cargoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
