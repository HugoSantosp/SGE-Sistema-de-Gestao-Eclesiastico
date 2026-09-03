package com.sg.shared.services;

import com.itextpdf.io.font.constants.StandardFonts;
import java.io.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import com.sg.membros.Membro;
import com.sg.financeiro.ContaPagar;
import com.sg.financeiro.ContaReceber;
import com.sg.escala.Escala;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service completo para geração de relatórios.
 *
 * Funcionalidades:
 * - Exportação PDF (iText 8)
 * - Exportação Excel (Apache POI)
 * - Exportação CSV
 * - Relatórios personalizados
 */
@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private static final DeviceRgb ORANGE = new DeviceRgb(249, 115, 22);
    private static final DeviceRgb RED = new DeviceRgb(239, 68, 68);
    private static final DeviceRgb GREEN = new DeviceRgb(34, 197, 94);
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(220, 220, 220);

    // ==================== PDF ====================

    /**
     * Gera relatório de membros em PDF
     */
    public byte[] generateMembersPdf(List<Membro> membros) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);

        PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont bodyFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Título
        Paragraph title = new Paragraph("Relatório de Membros")
                .setFont(titleFont).setFontSize(18).setFontColor(ORANGE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(title);

        // Data
        Paragraph date = new Paragraph("Gerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFont(bodyFont).setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(date);

        // Tabela
        float[] widths = {3, 4, 3, 3, 2};
        Table table = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();

        // Cabeçalho
        String[] headers = {"Nome", "Telefone", "Documento", "Endereço", "Situação"};
        for (String header : headers) {
            Cell cell = new Cell().add(new Paragraph(header).setFont(titleFont).setFontSize(10).setFontColor(WHITE));
            cell.setBackgroundColor(ORANGE).setPadding(6).setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(cell);
        }

        // Dados
        if (membros != null) {
            for (Membro membro : membros) {
                table.addCell(createDataCell(membro.getNome(), bodyFont));
                table.addCell(createDataCell(membro.getTelefone(), bodyFont));
                table.addCell(createDataCell(membro.getDocumento(), bodyFont));
                table.addCell(createDataCell(membro.getEndereco(), bodyFont));
                table.addCell(createDataCell(
                        membro.getSituacao() != null ? membro.getSituacao().name() : "-", bodyFont));
            }
        }

        document.add(table);

        // Rodapé
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph("Total de membros: " + (membros != null ? membros.size() : 0))
                .setFont(bodyFont).setFontSize(8).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    /**
     * Gera relatório financeiro em PDF
     */
    public byte[] generateFinanceReportPdf(List<ContaPagar> contasPagar, List<ContaReceber> contasReceber) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);

        PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont bodyFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Título
        document.add(new Paragraph("Relatório Financeiro")
                .setFont(titleFont).setFontSize(18).setFontColor(ORANGE)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

        // Período
        String periodo = LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ofPattern("dd/MM"))
                + " a " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        document.add(new Paragraph("Período: " + periodo)
                .setFont(bodyFont).setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

        // Resumo
        BigDecimal totalPagar = contasPagar != null
                ? contasPagar.stream().map(ContaPagar::getValor).reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;
        BigDecimal totalReceber = contasReceber != null
                ? contasReceber.stream().map(ContaReceber::getValor).reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;
        BigDecimal saldo = totalReceber.subtract(totalPagar);

        document.add(new Paragraph("Total a Pagar: R$ " + String.format("%.2f", totalPagar))
                .setFont(titleFont).setFontSize(12).setMarginBottom(5));
        document.add(new Paragraph("Total a Receber: R$ " + String.format("%.2f", totalReceber))
                .setFont(titleFont).setFontSize(12).setMarginBottom(5));
        document.add(new Paragraph("Saldo: R$ " + String.format("%.2f", saldo))
                .setFont(titleFont).setFontSize(12).setMarginBottom(20));

        // Tabela contas a pagar
        if (contasPagar != null && !contasPagar.isEmpty()) {
            document.add(new Paragraph("Contas a Pagar").setFont(titleFont).setFontSize(14).setMarginBottom(10));

            Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2, 2})).useAllAvailableWidth();
            String[] headers = {"Descrição", "Valor", "Vencimento", "Status"};
            for (String header : headers) {
                Cell cell = new Cell().add(new Paragraph(header).setFont(titleFont).setFontSize(10).setFontColor(WHITE));
                cell.setBackgroundColor(RED).setPadding(6).setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(cell);
            }

            for (ContaPagar conta : contasPagar) {
                table.addCell(createDataCell(conta.getDescricao(), bodyFont));
                table.addCell(createDataCell("R$ " + String.format("%.2f", conta.getValor()), bodyFont));
                table.addCell(createDataCell(conta.getVencimento() != null ? conta.getVencimento().toString() : "-", bodyFont));
                table.addCell(createDataCell(conta.getStatus() != null ? conta.getStatus().name() : "-", bodyFont));
            }

            document.add(table);
        }

        document.close();
        return baos.toByteArray();
    }

    /**
     * Gera relatório de escalas em PDF
     */
    public byte[] generateScheduleReportPdf(List<Escala> escalas) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);

        PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont bodyFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Título
        document.add(new Paragraph("Relatório de Escalas")
                .setFont(titleFont).setFontSize(18).setFontColor(ORANGE)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

        document.add(new Paragraph("Gerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFont(bodyFont).setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

        // Tabela
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 3, 2, 2})).useAllAvailableWidth();

        String[] headers = {"Título", "Ministério", "Status", "Confirmações"};
        for (String header : headers) {
            Cell cell = new Cell().add(new Paragraph(header).setFont(titleFont).setFontSize(10).setFontColor(WHITE));
            cell.setBackgroundColor(GREEN).setPadding(6).setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(cell);
        }

        if (escalas != null) {
            for (Escala escala : escalas) {
                table.addCell(createDataCell(escala.getTitulo(), bodyFont));
                table.addCell(createDataCell(
                        escala.getMinisterioId() != null ? "Ministério " + escala.getMinisterioId() : "Geral",
                        bodyFont));
                table.addCell(createDataCell(escala.isAberta() ? "Aberta" : "Fechada", bodyFont));
                table.addCell(createDataCell(
                        String.valueOf(escala.getConfirmacoes() != null ? escala.getConfirmacoes().size() : 0),
                        bodyFont));
            }
        }

        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    // ==================== EXCEL ====================

    /**
     * Gera relatório em Excel
     */
    public byte[] generateExcelReport(String sheetName, List<String> headers, List<Map<String, Object>> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        // Estilo do cabeçalho
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 249, (byte) 115, (byte) 22}, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // Criar cabeçalho
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            org.apache.poi.ss.usermodel.Cell poiCell = headerRow.createCell(i);
            poiCell.setCellValue(headers.get(i));
            poiCell.setCellStyle(headerStyle);
        }

        // Estilo dos dados
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);

        // Criar dados
        int rowNum = 1;
        for (Map<String, Object> rowData : data) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (String header : headers) {
                org.apache.poi.ss.usermodel.Cell poiCell = row.createCell(colNum++);
                Object value = rowData.get(header);
                if (value != null) {
                    if (value instanceof Number) {
                        poiCell.setCellValue(((Number) value).doubleValue());
                    } else {
                        poiCell.setCellValue(value.toString());
                    }
                }
                poiCell.setCellStyle(dataStyle);
            }
        }

        // Ajustar largura das colunas
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    // ==================== CSV ====================

    /**
     * Gera relatório em CSV
     */
    public String generateCsvReport(List<String> headers, List<Map<String, Object>> data) {
        StringBuilder csv = new StringBuilder();

        csv.append(String.join(",", headers)).append("\n");

        for (Map<String, Object> rowData : data) {
            String[] row = new String[headers.size()];
            for (int i = 0; i < headers.size(); i++) {
                Object value = rowData.get(headers.get(i));
                row[i] = value != null ? escapeCsv(value.toString()) : "";
            }
            csv.append(String.join(",", row)).append("\n");
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ==================== HELPERS ====================

    private Cell createDataCell(String content, PdfFont font) {
        Cell cell = new Cell().add(new Paragraph(content != null ? content : "-").setFont(font).setFontSize(10));
        cell.setPadding(5).setBorder(new SolidBorder(LIGHT_GRAY, 0.5f));
        return cell;
    }

    /**
     * Gera nome do arquivo baseado no tipo e data
     */
    public String generateFilename(String reportType, String extension) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("relatorio_%s_%s.%s", reportType.toLowerCase().replace(" ", "_"), date, extension);
    }
}
