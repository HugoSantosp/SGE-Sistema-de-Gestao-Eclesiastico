package com.sg.membros;

import com.sg.membros.dto.MembroRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/membros")
@Tag(name = "Membros", description = "CRUD de membros da igreja")
public class MembroController {

    private final MembroService membroService;

    public MembroController(MembroService membroService) {
        this.membroService = membroService;
    }

    @GetMapping
    @Operation(summary = "Listar membros")
    public ResponseEntity<List<Membro>> listar() {
        return ResponseEntity.ok(membroService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar membro por ID")
    public ResponseEntity<Membro> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(membroService.buscarPorId(id));
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Obter estatísticas de membros")
    public ResponseEntity<Map<String, Long>> estatisticas() {
        return ResponseEntity.ok(Map.of(
                "total", membroService.contarTotal(),
                "ativos", membroService.contarAtivos(),
                "inativos", membroService.contarInativos()
        ));
    }

    @PostMapping
    @Operation(summary = "Criar novo membro")
    public ResponseEntity<Membro> criar(@RequestBody @Valid MembroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membroService.salvar(toEntity(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar membro")
    public ResponseEntity<Membro> atualizar(@PathVariable Long id, @RequestBody @Valid MembroRequestDTO dto) {
        return ResponseEntity.ok(membroService.atualizar(id, toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar membro")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        membroService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private Membro toEntity(MembroRequestDTO dto) {
        return Membro.builder()
                .nome(dto.nome()).documento(dto.documento()).telefone(dto.telefone())
                .endereco(dto.endereco()).foto(dto.foto()).dataNasc(dto.dataNasc())
                .situacao(dto.situacao()).funcaoId(dto.funcaoId())
                .ministerioId(dto.ministerioId())
                .dataBatismo(dto.dataBatismo()).obs(dto.obs()).build();
    }
}
