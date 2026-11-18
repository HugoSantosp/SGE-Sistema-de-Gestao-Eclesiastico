package com.sg.financeiro;

import com.sg.financeiro.dto.DoacaoRequestDTO;
import com.sg.shared.enums.FormaPagamento;
import com.sg.shared.enums.TipoDoacao;
import com.sg.shared.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/financeiro/doacoes")
@Tag(name = "Doações", description = "Dízimos, ofertas e primícias")
public class DoacaoController {

    private final DoacaoService doacaoService;

    public DoacaoController(DoacaoService doacaoService) {
        this.doacaoService = doacaoService;
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping
    @Operation(summary = "Listar doações",
            description = "Filtros opcionais por tipo, período, forma de pagamento e categoria")
    public ResponseEntity<List<Doacao>> listar(
            @RequestParam(required = false) TipoDoacao tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) FormaPagamento formaPagamento,
            @RequestParam(required = false) Long categoriaId) {
        return ResponseEntity.ok(doacaoService.listar(tipo, inicio, fim, formaPagamento, categoriaId));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/{id}")
    @Operation(summary = "Buscar doação por ID")
    public ResponseEntity<Doacao> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(doacaoService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PostMapping
    @Operation(summary = "Registrar doação")
    public ResponseEntity<Doacao> criar(@RequestBody @Valid DoacaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doacaoService.salvar(dto, UsuarioAutenticado.id()));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar doação", description = "Justificativa obrigatória")
    public ResponseEntity<Doacao> cancelar(@PathVariable Long id, @RequestParam String justificativa) {
        return ResponseEntity.ok(doacaoService.cancelar(id, justificativa));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar doação")
    public ResponseEntity<Doacao> atualizar(@PathVariable Long id, @RequestBody @Valid DoacaoRequestDTO dto) {
        return ResponseEntity.ok(doacaoService.atualizar(id, dto));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir doação")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        doacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
