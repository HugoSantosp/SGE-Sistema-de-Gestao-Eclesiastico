package com.sg.financeiro;

import com.sg.financeiro.dto.ContaPagarRequestDTO;
import com.sg.financeiro.dto.ContaReceberRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/financeiro")
@Tag(name = "Financeiro", description = "Gestão de contas a pagar e receber")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    // ------ CONTAS A PAGAR ------
    @GetMapping("/contas-pagar")
    @Operation(summary = "Listar contas a pagar")
    public ResponseEntity<List<ContaPagar>> listarContasPagar() {
        return ResponseEntity.ok(financeiroService.listarContasPagar());
    }

    @GetMapping("/contas-pagar/{id}")
    @Operation(summary = "Buscar conta a pagar por ID")
    public ResponseEntity<ContaPagar> buscarContaPagar(@PathVariable Long id) {
        return ResponseEntity.ok(financeiroService.buscarContaPagar(id));
    }

    @PostMapping("/contas-pagar")
    @Operation(summary = "Criar conta a pagar")
    public ResponseEntity<ContaPagar> criarContaPagar(@RequestBody @Valid ContaPagarRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeiroService.salvarContaPagar(toEntity(dto)));
    }

    @PatchMapping("/contas-pagar/{id}/baixar")
    @Operation(summary = "Baixar conta a pagar")
    public ResponseEntity<ContaPagar> baixarContaPagar(@PathVariable Long id) {
        return ResponseEntity.ok(financeiroService.baixarContaPagar(id));
    }

    @DeleteMapping("/contas-pagar/{id}")
    @Operation(summary = "Deletar conta a pagar")
    public ResponseEntity<Void> deletarContaPagar(@PathVariable Long id) {
        financeiroService.deletarContaPagar(id);
        return ResponseEntity.noContent().build();
    }

    // ------ CONTAS A RECEBER ------
    @GetMapping("/contas-receber")
    @Operation(summary = "Listar contas a receber")
    public ResponseEntity<List<ContaReceber>> listarContasReceber() {
        return ResponseEntity.ok(financeiroService.listarContasReceber());
    }

    @GetMapping("/contas-receber/{id}")
    @Operation(summary = "Buscar conta a receber por ID")
    public ResponseEntity<ContaReceber> buscarContaReceber(@PathVariable Long id) {
        return ResponseEntity.ok(financeiroService.buscarContaReceber(id));
    }

    @PostMapping("/contas-receber")
    @Operation(summary = "Criar conta a receber")
    public ResponseEntity<ContaReceber> criarContaReceber(@RequestBody @Valid ContaReceberRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeiroService.salvarContaReceber(toEntity(dto)));
    }

    @PatchMapping("/contas-receber/{id}/receber")
    @Operation(summary = "Receber conta")
    public ResponseEntity<ContaReceber> receberConta(@PathVariable Long id) {
        return ResponseEntity.ok(financeiroService.receberConta(id));
    }

    @DeleteMapping("/contas-receber/{id}")
    @Operation(summary = "Deletar conta a receber")
    public ResponseEntity<Void> deletarContaReceber(@PathVariable Long id) {
        financeiroService.deletarContaReceber(id);
        return ResponseEntity.noContent().build();
    }

    private ContaPagar toEntity(ContaPagarRequestDTO dto) {
        return ContaPagar.builder()
                .descricao(dto.descricao()).fornecedorId(dto.fornecedorId())
                .valor(dto.valor()).vencimento(dto.vencimento())
                .usuarioCadId(dto.usuarioCadId()).frequencia(dto.frequencia())
                .status(dto.status()).arquivo(dto.arquivo()).build();
    }

    private ContaReceber toEntity(ContaReceberRequestDTO dto) {
        return ContaReceber.builder()
                .descricao(dto.descricao()).valor(dto.valor())
                .vencimento(dto.vencimento()).frequencia(dto.frequencia())
                .status(dto.status())
                .contribuinte(dto.contribuinte()).build();
    }
}
