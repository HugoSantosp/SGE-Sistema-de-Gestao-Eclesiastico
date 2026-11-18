package com.sg.financeiro;

import com.sg.financeiro.dto.AlterarStatusRequestDTO;
import com.sg.financeiro.dto.ContaPagarRequestDTO;
import com.sg.financeiro.dto.ContaReceberRequestDTO;
import com.sg.financeiro.dto.PagamentoRequestDTO;
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
@RequestMapping("/api/financeiro")
@Tag(name = "Financeiro", description = "Gestão de contas a pagar e receber")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    // ------ CONTAS A PAGAR ------
    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/contas-pagar")
    @Operation(summary = "Listar contas a pagar")
    public ResponseEntity<List<ContaPagar>> listarContasPagar() {
        return ResponseEntity.ok(financeiroService.listarContasPagar());
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/contas-pagar/{id}")
    @Operation(summary = "Buscar conta a pagar por ID")
    public ResponseEntity<ContaPagar> buscarContaPagar(@PathVariable Long id) {
        return ResponseEntity.ok(financeiroService.buscarContaPagar(id));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PostMapping("/contas-pagar")
    @Operation(summary = "Criar conta a pagar")
    public ResponseEntity<ContaPagar> criarContaPagar(@RequestBody @Valid ContaPagarRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeiroService.salvarContaPagar(toEntity(dto)));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PutMapping("/contas-pagar/{id}")
    @Operation(summary = "Atualizar conta a pagar")
    public ResponseEntity<ContaPagar> atualizarContaPagar(@PathVariable Long id,
                                                          @RequestBody @Valid ContaPagarRequestDTO dto) {
        return ResponseEntity.ok(financeiroService.atualizarContaPagar(id, toEntity(dto)));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PatchMapping("/contas-pagar/{id}/baixar")
    @Operation(summary = "Baixar conta a pagar")
    public ResponseEntity<ContaPagar> baixarContaPagar(@PathVariable Long id) {
        return ResponseEntity.ok(financeiroService.baixarContaPagar(id));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @PatchMapping("/contas-pagar/{id}/status")
    @Operation(summary = "Alterar status da conta a pagar",
            description = "Tesoureiro e bispo podem marcar como pendente, vencida ou paga. Cancelamento exige justificativa.")
    public ResponseEntity<ContaPagar> alterarStatusContaPagar(@PathVariable Long id,
                                                              @RequestBody @Valid AlterarStatusRequestDTO dto) {
        return ResponseEntity.ok(financeiroService.alterarStatusContaPagar(id, dto.status()));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PatchMapping("/contas-pagar/{id}/cancelar")
    @Operation(summary = "Cancelar conta a pagar", description = "Justificativa obrigatória")
    public ResponseEntity<ContaPagar> cancelarContaPagar(@PathVariable Long id,
                                                         @RequestParam String justificativa) {
        return ResponseEntity.ok(
                financeiroService.cancelarContaPagar(id, justificativa, UsuarioAutenticado.id()));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/contas-pagar/{id}/pagamentos")
    @Operation(summary = "Listar pagamentos da conta a pagar")
    public ResponseEntity<List<PagamentoDespesa>> listarPagamentos(@PathVariable Long id) {
        return ResponseEntity.ok(financeiroService.listarPagamentos(id));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PostMapping("/contas-pagar/{id}/pagamentos")
    @Operation(summary = "Registrar pagamento integral ou parcial")
    public ResponseEntity<ContaPagar> registrarPagamento(@PathVariable Long id,
                                                         @RequestBody @Valid PagamentoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(financeiroService.registrarPagamento(id, dto, UsuarioAutenticado.id()));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @DeleteMapping("/contas-pagar/{id}")
    @Operation(summary = "Deletar conta a pagar")
    public ResponseEntity<Void> deletarContaPagar(@PathVariable Long id) {
        financeiroService.deletarContaPagar(id);
        return ResponseEntity.noContent().build();
    }

    // ------ CONTAS A RECEBER ------
    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/contas-receber")
    @Operation(summary = "Listar contas a receber")
    public ResponseEntity<List<ContaReceber>> listarContasReceber() {
        return ResponseEntity.ok(financeiroService.listarContasReceber());
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/contas-receber/{id}")
    @Operation(summary = "Buscar conta a receber por ID")
    public ResponseEntity<ContaReceber> buscarContaReceber(@PathVariable Long id) {
        return ResponseEntity.ok(financeiroService.buscarContaReceber(id));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PostMapping("/contas-receber")
    @Operation(summary = "Criar conta a receber")
    public ResponseEntity<ContaReceber> criarContaReceber(@RequestBody @Valid ContaReceberRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeiroService.salvarContaReceber(toEntity(dto)));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PutMapping("/contas-receber/{id}")
    @Operation(summary = "Atualizar conta a receber")
    public ResponseEntity<ContaReceber> atualizarContaReceber(@PathVariable Long id,
                                                              @RequestBody @Valid ContaReceberRequestDTO dto) {
        return ResponseEntity.ok(financeiroService.atualizarContaReceber(id, toEntity(dto)));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PatchMapping("/contas-receber/{id}/receber")
    @Operation(summary = "Receber conta")
    public ResponseEntity<ContaReceber> receberConta(@PathVariable Long id) {
        return ResponseEntity.ok(financeiroService.receberConta(id));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @PatchMapping("/contas-receber/{id}/status")
    @Operation(summary = "Alterar status da conta a receber",
            description = "Tesoureiro e bispo podem marcar como pendente, vencida ou paga. Cancelamento exige justificativa.")
    public ResponseEntity<ContaReceber> alterarStatusContaReceber(@PathVariable Long id,
                                                                  @RequestBody @Valid AlterarStatusRequestDTO dto) {
        return ResponseEntity.ok(financeiroService.alterarStatusContaReceber(id, dto.status()));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @PatchMapping("/contas-receber/{id}/cancelar")
    @Operation(summary = "Cancelar conta a receber", description = "Justificativa obrigatória")
    public ResponseEntity<ContaReceber> cancelarContaReceber(@PathVariable Long id,
                                                             @RequestParam String justificativa) {
        return ResponseEntity.ok(
                financeiroService.cancelarContaReceber(id, justificativa, UsuarioAutenticado.id()));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','TESOUREIRO')")
    @DeleteMapping("/contas-receber/{id}")
    @Operation(summary = "Deletar conta a receber")
    public ResponseEntity<Void> deletarContaReceber(@PathVariable Long id) {
        financeiroService.deletarContaReceber(id);
        return ResponseEntity.noContent().build();
    }

    private ContaPagar toEntity(ContaPagarRequestDTO dto) {
        Long responsavel = UsuarioAutenticado.id();
        return ContaPagar.builder()
                .descricao(dto.descricao()).fornecedorId(dto.fornecedorId())
                .valor(dto.valor()).vencimento(dto.vencimento())
                .dataEmissao(dto.dataEmissao()).categoriaId(dto.categoriaId())
                .usuarioCadId(responsavel != null ? responsavel : dto.usuarioCadId())
                .frequencia(dto.frequencia())
                .status(dto.status()).observacao(dto.observacao())
                .arquivo(dto.arquivo()).build();
    }

    private ContaReceber toEntity(ContaReceberRequestDTO dto) {
        return ContaReceber.builder()
                .descricao(dto.descricao()).valor(dto.valor())
                .vencimento(dto.vencimento()).frequencia(dto.frequencia())
                .status(dto.status())
                .contribuinte(dto.contribuinte())
                .categoriaId(dto.categoriaId())
                .formaPagamento(dto.formaPagamento())
                .observacao(dto.observacao()).build();
    }
}
