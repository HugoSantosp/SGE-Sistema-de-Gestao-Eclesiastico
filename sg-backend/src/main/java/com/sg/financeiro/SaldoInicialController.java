package com.sg.financeiro;

import com.sg.financeiro.dto.SaldoInicialRequestDTO;
import com.sg.shared.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financeiro/saldo-inicial")
@Tag(name = "Saldo inicial", description = "Saldo inicial configurável com histórico")
public class SaldoInicialController {

    private final SaldoInicialService saldoInicialService;

    public SaldoInicialController(SaldoInicialService saldoInicialService) {
        this.saldoInicialService = saldoInicialService;
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping
    @Operation(summary = "Histórico de saldos iniciais")
    public ResponseEntity<List<SaldoInicial>> historico() {
        return ResponseEntity.ok(saldoInicialService.historico());
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/vigente")
    @Operation(summary = "Saldo inicial vigente")
    public ResponseEntity<SaldoInicial> vigente() {
        return ResponseEntity.ok(saldoInicialService.vigente());
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PostMapping
    @Operation(summary = "Registrar novo saldo inicial", description = "Gera registro no histórico")
    public ResponseEntity<SaldoInicial> registrar(@RequestBody @Valid SaldoInicialRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saldoInicialService.registrar(dto, UsuarioAutenticado.id()));
    }
}
