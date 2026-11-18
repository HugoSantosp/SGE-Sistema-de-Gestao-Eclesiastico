package com.sg.financeiro;

import com.sg.financeiro.dto.DashboardResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/financeiro/dashboard")
@Tag(name = "Dashboard financeira", description = "Indicadores consolidados da situação financeira")
public class DashboardFinanceiroController {

    private final DashboardFinanceiroService dashboardService;

    public DashboardFinanceiroController(DashboardFinanceiroService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PreAuthorize("hasAnyRole('PASTOR_PRESIDENTE','PASTOR_AUXILIAR','TESOUREIRO')")
    @GetMapping
    @Operation(summary = "Indicadores financeiros do período",
            description = "Todos os valores são calculados no backend")
    public ResponseEntity<DashboardResponseDTO> dashboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(dashboardService.calcular(inicio, fim));
    }
}
