package com.sg.shared.config;

import com.sg.membros.Membro;
import com.sg.membros.MembroRepository;
import com.sg.financeiro.ContaPagar;
import com.sg.financeiro.ContaPagarRepository;
import com.sg.financeiro.ContaReceber;
import com.sg.financeiro.ContaReceberRepository;
import com.sg.escala.Escala;
import com.sg.escala.EscalaRepository;
import com.sg.shared.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller para geração e exportação de relatórios.
 *
 * Endpoints:
 * - GET /api/reports/members/pdf - Relatório de membros em PDF
 * - GET /api/reports/members/excel - Relatório de membros em Excel
 * - GET /api/reports/finance/pdf - Relatório financeiro em PDF
 * - GET /api/reports/schedule/pdf - Relatório de escalas em PDF
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Relatórios", description = "Geração e exportação de relatórios")
public class ReportController {

    private final ReportService reportService;
    private final MembroRepository membroRepository;
    private final ContaPagarRepository contaPagarRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final EscalaRepository escalaRepository;

    public ReportController(
            ReportService reportService,
            MembroRepository membroRepository,
            ContaPagarRepository contaPagarRepository,
            ContaReceberRepository contaReceberRepository,
            EscalaRepository escalaRepository) {
        this.reportService = reportService;
        this.membroRepository = membroRepository;
        this.contaPagarRepository = contaPagarRepository;
        this.contaReceberRepository = contaReceberRepository;
        this.escalaRepository = escalaRepository;
    }

    @GetMapping("/members/pdf")
    @Operation(summary = "Gerar relatório de membros em PDF")
    public ResponseEntity<byte[]> generateMembersPdf() {
        try {
            List<Membro> membros = membroRepository.findAll();
            byte[] pdfBytes = reportService.generateMembersPdf(membros);

            String filename = reportService.generateFilename("membros", "pdf");

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/members/excel")
    @Operation(summary = "Gerar relatório de membros em Excel")
    public ResponseEntity<byte[]> generateMembersExcel() {
        try {
            List<Membro> membros = membroRepository.findAll();

            List<String> headers = List.of("Nome", "Telefone", "Documento", "Endereço", "Situação");
            List<Map<String, Object>> data = membros.stream().map(m -> {
                Map<String, Object> row = new HashMap<>();
                row.put("Nome", m.getNome());
                row.put("Telefone", m.getTelefone());
                row.put("Documento", m.getDocumento());
                row.put("Endereço", m.getEndereco());
                row.put("Situação", m.getSituacao() != null ? m.getSituacao().name() : "-");
                return row;
            }).collect(Collectors.toList());

            byte[] excelBytes = reportService.generateExcelReport("Membros", headers, data);

            String filename = reportService.generateFilename("membros", "xlsx");

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(excelBytes.length)
                .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/finance/pdf")
    @Operation(summary = "Gerar relatório financeiro em PDF")
    public ResponseEntity<byte[]> generateFinancePdf() {
        try {
            List<ContaPagar> contasPagar = contaPagarRepository.findAll();
            List<ContaReceber> contasReceber = contaReceberRepository.findAll();

            byte[] pdfBytes = reportService.generateFinanceReportPdf(contasPagar, contasReceber);

            String filename = reportService.generateFilename("financeiro", "pdf");

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/schedule/pdf")
    @Operation(summary = "Gerar relatório de escalas em PDF")
    public ResponseEntity<byte[]> generateSchedulePdf() {
        try {
            List<Escala> escalas = escalaRepository.findAll();

            byte[] pdfBytes = reportService.generateScheduleReportPdf(escalas);

            String filename = reportService.generateFilename("escalas", "pdf");

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/schedule/excel")
    @Operation(summary = "Gerar relatório de escalas em Excel")
    public ResponseEntity<byte[]> generateScheduleExcel() {
        try {
            List<Escala> escalas = escalaRepository.findAll();

            List<String> headers = List.of("Título", "Ministério", "Status", "Confirmações");
            List<Map<String, Object>> data = escalas.stream().map(e -> {
                Map<String, Object> row = new HashMap<>();
                row.put("Título", e.getTitulo());
                row.put("Ministério", e.getMinisterioId() != null ? "Ministério " + e.getMinisterioId() : "Geral");
                row.put("Status", e.isAberta() ? "Aberta" : "Fechada");
                row.put("Confirmações", e.getConfirmacoes() != null ? e.getConfirmacoes().size() : 0);
                return row;
            }).collect(Collectors.toList());

            byte[] excelBytes = reportService.generateExcelReport("Escalas", headers, data);

            String filename = reportService.generateFilename("escalas", "xlsx");

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(excelBytes.length)
                .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/available")
    @Operation(summary = "Listar relatórios disponíveis")
    public ResponseEntity<List<Map<String, String>>> getAvailableReports() {
        List<Map<String, String>> reports = List.of(
            Map.of("id", "members-pdf", "name", "Relatório de Membros (PDF)", "endpoint", "/api/reports/members/pdf"),
            Map.of("id", "members-excel", "name", "Relatório de Membros (Excel)", "endpoint", "/api/reports/members/excel"),
            Map.of("id", "finance-pdf", "name", "Relatório Financeiro (PDF)", "endpoint", "/api/reports/finance/pdf"),
            Map.of("id", "schedule-pdf", "name", "Relatório de Escalas (PDF)", "endpoint", "/api/reports/schedule/pdf"),
            Map.of("id", "schedule-excel", "name", "Relatório de Escalas (Excel)", "endpoint", "/api/reports/schedule/excel")
        );

        return ResponseEntity.ok(reports);
    }
}
