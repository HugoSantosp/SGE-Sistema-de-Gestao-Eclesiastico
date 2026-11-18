package com.sg.financeiro;

import com.sg.financeiro.dto.RelatorioFinanceiroDTO;
import com.sg.shared.services.ReportService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelatorioFinanceiroExportService - PDF e Excel")
class RelatorioFinanceiroExportServiceTest {

    @Mock private RelatorioFinanceiroService relatorioService;

    private RelatorioFinanceiroExportService exportService;

    private final LocalDate inicio = LocalDate.of(2026, 11, 1);
    private final LocalDate fim = LocalDate.of(2026, 11, 30);

    private RelatorioFinanceiroDTO cabecalho(String titulo) {
        return new RelatorioFinanceiroDTO(titulo, "Igreja Central", inicio, fim,
                "Hugo Santos", LocalDate.of(2026, 11, 18));
    }

    @BeforeEach
    void setUp() {
        exportService = new RelatorioFinanceiroExportService(relatorioService, new ReportService());
    }

    @Test
    @DisplayName("PDF de receitas é gerado com assinatura válida")
    void receitasPdfValido() throws Exception {
        when(relatorioService.receitas(inicio, fim)).thenReturn(
                new RelatorioFinanceiroDTO.RelatorioReceitas(
                        cabecalho("Relatório de Receitas"),
                        List.of(new RelatorioFinanceiroDTO.ReceitaItem(
                                LocalDate.of(2026, 11, 10), "Dízimo", "Dízimos", "PIX",
                                new BigDecimal("300.00"))),
                        new BigDecimal("300.00")));

        byte[] pdf = exportService.receitasPdf(inicio, fim);

        assertTrue(pdf.length > 0);
        assertEquals("%PDF", new String(pdf, 0, 4));

        String texto = extrairTexto(pdf);
        assertTrue(texto.contains("Igreja Central"), "nome da igreja no PDF");
        assertTrue(texto.contains("Relatório de Receitas"), "título no PDF");
        assertTrue(texto.contains("Dízimo"), "item no PDF");
        assertTrue(texto.contains("300,00"), "total formatado no PDF");
    }

    /** Lê o PDF gerado de volta para confirmar que o conteúdo foi realmente renderizado. */
    private String extrairTexto(byte[] pdf) throws Exception {
        try (com.itextpdf.kernel.pdf.PdfDocument doc =
                     new com.itextpdf.kernel.pdf.PdfDocument(
                             new com.itextpdf.kernel.pdf.PdfReader(new ByteArrayInputStream(pdf)))) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                sb.append(com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
                        .getTextFromPage(doc.getPage(i)));
            }
            return sb.toString();
        }
    }

    @Test
    @DisplayName("PDF de despesas é gerado com assinatura válida")
    void despesasPdfValido() throws Exception {
        when(relatorioService.despesas(inicio, fim)).thenReturn(
                new RelatorioFinanceiroDTO.RelatorioDespesas(
                        cabecalho("Relatório de Despesas"),
                        List.of(new RelatorioFinanceiroDTO.DespesaItem(
                                "Aluguel", "Aluguel", "Imobiliária", LocalDate.of(2026, 11, 5),
                                new BigDecimal("1000.00"), new BigDecimal("400.00"),
                                new BigDecimal("600.00"), "PARCIALMENTE_PAGA")),
                        new BigDecimal("1000.00"), new BigDecimal("400.00"), new BigDecimal("600.00")));

        byte[] pdf = exportService.despesasPdf(inicio, fim);

        assertEquals("%PDF", new String(pdf, 0, 4));
    }

    @Test
    @DisplayName("PDF de fluxo de caixa é gerado com assinatura válida")
    void fluxoCaixaPdfValido() throws Exception {
        when(relatorioService.fluxoCaixa(inicio, fim)).thenReturn(
                new RelatorioFinanceiroDTO.RelatorioFluxoCaixa(
                        cabecalho("Fluxo de Caixa"),
                        new BigDecimal("1000.00"), new BigDecimal("500.00"), new BigDecimal("200.00"),
                        List.of(new RelatorioFinanceiroDTO.MovimentoCaixa(
                                LocalDate.of(2026, 11, 10), "Dízimo",
                                new BigDecimal("500.00"), BigDecimal.ZERO, new BigDecimal("1500.00"))),
                        new BigDecimal("1300.00")));

        byte[] pdf = exportService.fluxoCaixaPdf(inicio, fim);

        assertEquals("%PDF", new String(pdf, 0, 4));
    }

    @Test
    @DisplayName("PDF do demonstrativo é gerado com assinatura válida")
    void demonstrativoPdfValido() throws Exception {
        when(relatorioService.demonstrativo(inicio, fim)).thenReturn(
                new RelatorioFinanceiroDTO.RelatorioDemonstrativo(
                        cabecalho("Demonstrativo Financeiro"),
                        new BigDecimal("800.00"), new BigDecimal("300.00"), new BigDecimal("500.00"),
                        new BigDecimal("2000.00"), new BigDecimal("2500.00")));

        byte[] pdf = exportService.demonstrativoPdf(inicio, fim);

        assertEquals("%PDF", new String(pdf, 0, 4));
    }

    @Test
    @DisplayName("Excel de receitas traz cabeçalho, dados, filtro e total")
    void receitasExcelComFiltroETotal() throws Exception {
        when(relatorioService.receitas(inicio, fim)).thenReturn(
                new RelatorioFinanceiroDTO.RelatorioReceitas(
                        cabecalho("Relatório de Receitas"),
                        List.of(
                                new RelatorioFinanceiroDTO.ReceitaItem(
                                        LocalDate.of(2026, 11, 10), "Dízimo", "Dízimos", "PIX",
                                        new BigDecimal("300.00")),
                                new RelatorioFinanceiroDTO.ReceitaItem(
                                        LocalDate.of(2026, 11, 12), "Oferta", "Ofertas", "DINHEIRO",
                                        new BigDecimal("200.00"))),
                        new BigDecimal("500.00")));

        byte[] excel = exportService.receitasExcel(inicio, fim);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            Sheet sheet = workbook.getSheet("Receitas");
            assertNotNull(sheet);
            assertEquals("Data", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Valor", sheet.getRow(0).getCell(4).getStringCellValue());
            assertEquals(300.00, sheet.getRow(1).getCell(4).getNumericCellValue(), 0.001);
            assertEquals(200.00, sheet.getRow(2).getCell(4).getNumericCellValue(), 0.001);
            assertEquals("TOTAL", sheet.getRow(3).getCell(0).getStringCellValue());
            assertEquals(500.00, sheet.getRow(3).getCell(4).getNumericCellValue(), 0.001);
            assertNotNull(((org.apache.poi.xssf.usermodel.XSSFSheet) sheet)
                    .getCTWorksheet().getAutoFilter(), "filtro nas colunas");
        }
    }

    @Test
    @DisplayName("Excel de despesas traz os três totais")
    void despesasExcelComTotais() throws Exception {
        when(relatorioService.despesas(inicio, fim)).thenReturn(
                new RelatorioFinanceiroDTO.RelatorioDespesas(
                        cabecalho("Relatório de Despesas"),
                        List.of(new RelatorioFinanceiroDTO.DespesaItem(
                                "Aluguel", "Aluguel", "Imobiliária", LocalDate.of(2026, 11, 5),
                                new BigDecimal("1000.00"), new BigDecimal("400.00"),
                                new BigDecimal("600.00"), "PARCIALMENTE_PAGA")),
                        new BigDecimal("1000.00"), new BigDecimal("400.00"), new BigDecimal("600.00")));

        byte[] excel = exportService.despesasExcel(inicio, fim);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            Sheet sheet = workbook.getSheet("Despesas");
            Row total = sheet.getRow(2);
            assertEquals(1000.00, total.getCell(4).getNumericCellValue(), 0.001);
            assertEquals(400.00, total.getCell(5).getNumericCellValue(), 0.001);
            assertEquals(600.00, total.getCell(6).getNumericCellValue(), 0.001);
        }
    }

    @Test
    @DisplayName("Excel de fluxo de caixa mantém ordem cronológica")
    void fluxoCaixaExcelOrdenado() throws Exception {
        when(relatorioService.fluxoCaixa(inicio, fim)).thenReturn(
                new RelatorioFinanceiroDTO.RelatorioFluxoCaixa(
                        cabecalho("Fluxo de Caixa"),
                        new BigDecimal("1000.00"), new BigDecimal("500.00"), new BigDecimal("200.00"),
                        List.of(
                                new RelatorioFinanceiroDTO.MovimentoCaixa(
                                        LocalDate.of(2026, 11, 3), "Pagamento",
                                        BigDecimal.ZERO, new BigDecimal("200.00"), new BigDecimal("800.00")),
                                new RelatorioFinanceiroDTO.MovimentoCaixa(
                                        LocalDate.of(2026, 11, 10), "Dízimo",
                                        new BigDecimal("500.00"), BigDecimal.ZERO, new BigDecimal("1300.00"))),
                        new BigDecimal("1300.00")));

        byte[] excel = exportService.fluxoCaixaExcel(inicio, fim);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            Sheet sheet = workbook.getSheet("Fluxo de Caixa");
            assertEquals("Pagamento", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Dízimo", sheet.getRow(2).getCell(1).getStringCellValue());
            assertEquals(1300.00, sheet.getRow(3).getCell(4).getNumericCellValue(), 0.001);
        }
    }

    @Test
    @DisplayName("Excel do demonstrativo lista os cinco indicadores")
    void demonstrativoExcelIndicadores() throws Exception {
        when(relatorioService.demonstrativo(inicio, fim)).thenReturn(
                new RelatorioFinanceiroDTO.RelatorioDemonstrativo(
                        cabecalho("Demonstrativo Financeiro"),
                        new BigDecimal("800.00"), new BigDecimal("300.00"), new BigDecimal("500.00"),
                        new BigDecimal("2000.00"), new BigDecimal("2500.00")));

        byte[] excel = exportService.demonstrativoExcel(inicio, fim);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            Sheet sheet = workbook.getSheet("Demonstrativo");
            assertEquals(5, sheet.getLastRowNum());
            assertEquals("Total de receitas", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals(2500.00, sheet.getRow(5).getCell(1).getNumericCellValue(), 0.001);
        }
    }
}
