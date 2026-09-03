package com.sg.shared.config;

import com.sg.shared.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para download de relatórios gerados.
 */
@RestController
@RequestMapping("/api/reports/download")
@Tag(name = "Download de Relatórios", description = "Download de relatórios gerados")
public class ReportDownloadController {

    private final ReportService reportService;

    public ReportDownloadController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/{reportType}")
    @Operation(summary = "Baixar relatório por tipo")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable String reportType,
            @RequestParam(defaultValue = "pdf") String format) {

        try {
            byte[] reportBytes;
            String filename;

            switch (reportType.toLowerCase()) {
                case "members":
                    reportBytes = reportService.generateMembersPdf(null);
                    filename = reportService.generateFilename("membros", format);
                    break;
                case "finance":
                    reportBytes = reportService.generateFinanceReportPdf(null, null);
                    filename = reportService.generateFilename("financeiro", format);
                    break;
                case "schedule":
                    reportBytes = reportService.generateScheduleReportPdf(null);
                    filename = reportService.generateFilename("escalas", format);
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            MediaType mediaType = "pdf".equals(format) ?
                MediaType.APPLICATION_PDF : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(mediaType)
                .contentLength(reportBytes.length)
                .body(reportBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
