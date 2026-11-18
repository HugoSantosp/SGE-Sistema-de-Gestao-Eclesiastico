package com.sg.financeiro;

import com.sg.financeiro.dto.RelatorioFinanceiroDTO;
import com.sg.shared.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/financeiro/relatorios")
@Tag(name = "Relatórios financeiros", description = "Receitas, despesas, fluxo de caixa e demonstrativo")
public class RelatorioFinanceiroController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final RelatorioFinanceiroService relatorioService;
    private final RelatorioFinanceiroExportService exportService;
    private final ReportService reportService;

    public RelatorioFinanceiroController(RelatorioFinanceiroService relatorioService,
                                         RelatorioFinanceiroExportService exportService,
                                         ReportService reportService) {
        this.relatorioService = relatorioService;
        this.exportService = exportService;
        this.reportService = reportService;
    }

    // ==================== CONSULTA ====================

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/receitas")
    @Operation(summary = "Relatório de receitas do período")
    public ResponseEntity<RelatorioFinanceiroDTO.RelatorioReceitas> receitas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.receitas(inicio, fim));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/despesas")
    @Operation(summary = "Relatório de despesas do período")
    public ResponseEntity<RelatorioFinanceiroDTO.RelatorioDespesas> despesas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.despesas(inicio, fim));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/fluxo-caixa")
    @Operation(summary = "Fluxo de caixa em ordem cronológica")
    public ResponseEntity<RelatorioFinanceiroDTO.RelatorioFluxoCaixa> fluxoCaixa(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.fluxoCaixa(inicio, fim));
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/demonstrativo")
    @Operation(summary = "Demonstrativo financeiro do período")
    public ResponseEntity<RelatorioFinanceiroDTO.RelatorioDemonstrativo> demonstrativo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.demonstrativo(inicio, fim));
    }

    // ==================== EXPORTAÇÃO PDF ====================

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/receitas/export/pdf")
    @Operation(summary = "Exportar receitas em PDF")
    public ResponseEntity<byte[]> receitasPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) throws IOException {
        return arquivo(exportService.receitasPdf(inicio, fim), "receitas", "pdf", MediaType.APPLICATION_PDF);
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/despesas/export/pdf")
    @Operation(summary = "Exportar despesas em PDF")
    public ResponseEntity<byte[]> despesasPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) throws IOException {
        return arquivo(exportService.despesasPdf(inicio, fim), "despesas", "pdf", MediaType.APPLICATION_PDF);
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/fluxo-caixa/export/pdf")
    @Operation(summary = "Exportar fluxo de caixa em PDF")
    public ResponseEntity<byte[]> fluxoCaixaPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) throws IOException {
        return arquivo(exportService.fluxoCaixaPdf(inicio, fim), "fluxo_caixa", "pdf", MediaType.APPLICATION_PDF);
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/demonstrativo/export/pdf")
    @Operation(summary = "Exportar demonstrativo em PDF")
    public ResponseEntity<byte[]> demonstrativoPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) throws IOException {
        return arquivo(exportService.demonstrativoPdf(inicio, fim), "demonstrativo", "pdf", MediaType.APPLICATION_PDF);
    }

    // ==================== EXPORTAÇÃO EXCEL ====================

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/receitas/export/excel")
    @Operation(summary = "Exportar receitas em Excel")
    public ResponseEntity<byte[]> receitasExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) throws IOException {
        return arquivo(exportService.receitasExcel(inicio, fim), "receitas", "xlsx", XLSX);
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/despesas/export/excel")
    @Operation(summary = "Exportar despesas em Excel")
    public ResponseEntity<byte[]> despesasExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) throws IOException {
        return arquivo(exportService.despesasExcel(inicio, fim), "despesas", "xlsx", XLSX);
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/fluxo-caixa/export/excel")
    @Operation(summary = "Exportar fluxo de caixa em Excel")
    public ResponseEntity<byte[]> fluxoCaixaExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) throws IOException {
        return arquivo(exportService.fluxoCaixaExcel(inicio, fim), "fluxo_caixa", "xlsx", XLSX);
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping("/demonstrativo/export/excel")
    @Operation(summary = "Exportar demonstrativo em Excel")
    public ResponseEntity<byte[]> demonstrativoExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) throws IOException {
        return arquivo(exportService.demonstrativoExcel(inicio, fim), "demonstrativo", "xlsx", XLSX);
    }

    private ResponseEntity<byte[]> arquivo(byte[] conteudo, String tipo, String extensao, MediaType mediaType) {
        String nome = reportService.generateFilename(tipo, extensao);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
                .body(conteudo);
    }
}
