package com.sg.financeiro;

import com.sg.financeiro.dto.CategoriaRequestDTO;
import com.sg.shared.enums.TipoCategoria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financeiro/categorias")
@Tag(name = "Categorias financeiras", description = "Categorias de receitas e despesas")
public class CategoriaFinanceiraController {

    private final CategoriaFinanceiraService categoriaService;

    public CategoriaFinanceiraController(CategoriaFinanceiraService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping
    @Operation(summary = "Listar categorias", description = "Filtros opcionais por tipo e por situação")
    public ResponseEntity<List<CategoriaFinanceira>> listar(
            @RequestParam(required = false) TipoCategoria tipo,
            @RequestParam(required = false) Boolean apenasAtivas) {
        return ResponseEntity.ok(categoriaService.listar(tipo, apenasAtivas));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    public ResponseEntity<CategoriaFinanceira> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PostMapping
    @Operation(summary = "Criar categoria")
    public ResponseEntity<CategoriaFinanceira> criar(@RequestBody @Valid CategoriaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.criar(dto));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria")
    public ResponseEntity<CategoriaFinanceira> atualizar(@PathVariable Long id,
                                                         @RequestBody @Valid CategoriaRequestDTO dto) {
        return ResponseEntity.ok(categoriaService.atualizar(id, dto));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Ativar categoria")
    public ResponseEntity<CategoriaFinanceira> ativar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.alterarAtivo(id, true));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar categoria")
    public ResponseEntity<CategoriaFinanceira> desativar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.alterarAtivo(id, false));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria sem vínculo")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        categoriaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
