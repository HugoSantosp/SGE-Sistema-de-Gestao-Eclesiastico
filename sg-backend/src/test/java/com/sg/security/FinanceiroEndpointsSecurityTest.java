package com.sg.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Valida proteção, permissões e regras de erro dos endpoints financeiros.
 * Sobe o contexto completo com o perfil "test" (H2 em memória).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Financeiro - Segurança e permissões dos endpoints")
class FinanceiroEndpointsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String PERIODO = "?inicio=2026-11-01&fim=2026-11-30";

    // ===== Sem autenticação =====

    @Test
    @DisplayName("Sem token: dashboard é rejeitada")
    void semTokenDashboard() throws Exception {
        mockMvc.perform(get("/api/financeiro/dashboard" + PERIODO))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Sem token: categorias são rejeitadas")
    void semTokenCategorias() throws Exception {
        mockMvc.perform(get("/api/financeiro/categorias"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Sem token: relatórios são rejeitados")
    void semTokenRelatorios() throws Exception {
        mockMvc.perform(get("/api/financeiro/relatorios/receitas" + PERIODO))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Sem token: exportação é rejeitada")
    void semTokenExportacao() throws Exception {
        mockMvc.perform(get("/api/financeiro/relatorios/receitas/export/pdf" + PERIODO))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Sem token: saldo inicial é rejeitado")
    void semTokenSaldoInicial() throws Exception {
        mockMvc.perform(get("/api/financeiro/saldo-inicial"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Sem token: contas a pagar são rejeitadas")
    void semTokenContasPagar() throws Exception {
        mockMvc.perform(get("/api/financeiro/contas-pagar"))
                .andExpect(status().is4xxClientError());
    }

    // ===== Papel sem acesso financeiro =====

    @Test
    @WithMockUser(roles = "MEMBRO")
    @DisplayName("Membro não acessa dashboard")
    void membroNaoAcessaDashboard() throws Exception {
        mockMvc.perform(get("/api/financeiro/dashboard" + PERIODO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    @DisplayName("Membro não acessa relatórios")
    void membroNaoAcessaRelatorios() throws Exception {
        mockMvc.perform(get("/api/financeiro/relatorios/fluxo-caixa" + PERIODO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    @DisplayName("Membro não acessa categorias")
    void membroNaoAcessaCategorias() throws Exception {
        mockMvc.perform(get("/api/financeiro/categorias"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SECRETARIO")
    @DisplayName("Secretário não acessa dashboard financeira")
    void secretarioNaoAcessaDashboard() throws Exception {
        mockMvc.perform(get("/api/financeiro/dashboard" + PERIODO))
                .andExpect(status().isForbidden());
    }

    // ===== Tesoureiro =====

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Tesoureiro acessa dashboard")
    void tesoureiroAcessaDashboard() throws Exception {
        mockMvc.perform(get("/api/financeiro/dashboard" + PERIODO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indicadores").exists())
                .andExpect(jsonPath("$.evolucaoMensal").isArray());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Tesoureiro acessa os quatro relatórios")
    void tesoureiroAcessaRelatorios() throws Exception {
        mockMvc.perform(get("/api/financeiro/relatorios/receitas" + PERIODO))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/financeiro/relatorios/despesas" + PERIODO))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/financeiro/relatorios/fluxo-caixa" + PERIODO))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/financeiro/relatorios/demonstrativo" + PERIODO))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Tesoureiro exporta PDF e Excel")
    void tesoureiroExporta() throws Exception {
        mockMvc.perform(get("/api/financeiro/relatorios/receitas/export/pdf" + PERIODO))
                .andExpect(status().isOk())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertEquals(
                        "application/pdf", result.getResponse().getContentType()));

        mockMvc.perform(get("/api/financeiro/relatorios/receitas/export/excel" + PERIODO))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Tesoureiro cria categoria")
    void tesoureiroCriaCategoria() throws Exception {
        mockMvc.perform(post("/api/financeiro/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Categoria Teste Seguranca\",\"tipo\":\"DESPESA\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Categoria Teste Seguranca"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    // ===== Auxiliar: leitura sim, escrita não =====

    @Test
    @WithMockUser(roles = "PASTOR_AUXILIAR")
    @DisplayName("Auxiliar lê categorias mas não cria")
    void auxiliarLeNaoCria() throws Exception {
        mockMvc.perform(get("/api/financeiro/categorias"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/financeiro/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Bloqueada\",\"tipo\":\"RECEITA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASTOR_AUXILIAR")
    @DisplayName("Auxiliar não registra pagamento")
    void auxiliarNaoRegistraPagamento() throws Exception {
        mockMvc.perform(post("/api/financeiro/contas-pagar/1/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valorPago\":10.00,\"dataPagamento\":\"2026-11-10\","
                                + "\"formaPagamento\":\"PIX\"}"))
                .andExpect(status().isForbidden());
    }

    // ===== Regras de erro =====

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Período invertido retorna 422")
    void periodoInvertidoRetorna422() throws Exception {
        mockMvc.perform(get("/api/financeiro/dashboard?inicio=2026-11-30&fim=2026-11-01"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Data inválida retorna 400")
    void dataInvalidaRetorna400() throws Exception {
        mockMvc.perform(get("/api/financeiro/dashboard?inicio=xx&fim=2026-11-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Conta a pagar inexistente retorna 404")
    void contaPagarInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/financeiro/contas-pagar/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Cancelamento sem justificativa retorna 400")
    void cancelamentoSemJustificativaRetorna400() throws Exception {
        mockMvc.perform(patch("/api/financeiro/contas-pagar/999999/cancelar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Cancelamento de conta inexistente retorna 404")
    void cancelamentoContaInexistenteRetorna404() throws Exception {
        mockMvc.perform(patch("/api/financeiro/contas-pagar/999999/cancelar")
                        .param("justificativa", "erro de lançamento"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Pagamento com valor zero retorna 400")
    void pagamentoValorZeroRetorna400() throws Exception {
        mockMvc.perform(post("/api/financeiro/contas-pagar/999999/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valorPago\":0,\"dataPagamento\":\"2026-11-10\","
                                + "\"formaPagamento\":\"PIX\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Categoria sem nome retorna 400")
    void categoriaSemNomeRetorna400() throws Exception {
        mockMvc.perform(post("/api/financeiro/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"  \",\"tipo\":\"RECEITA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Categoria duplicada retorna 422")
    void categoriaDuplicadaRetorna422() throws Exception {
        String corpo = "{\"nome\":\"Categoria Duplicada\",\"tipo\":\"RECEITA\"}";

        mockMvc.perform(post("/api/financeiro/categorias")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/financeiro/categorias")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Excluir categoria inexistente retorna 404")
    void excluirCategoriaInexistenteRetorna404() throws Exception {
        mockMvc.perform(delete("/api/financeiro/categorias/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("PUT de conta a pagar inexistente retorna 404, não 405")
    void putContaPagarInexistenteRetorna404() throws Exception {
        mockMvc.perform(put("/api/financeiro/contas-pagar/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Teste\",\"valor\":100.00,"
                                + "\"vencimento\":\"2026-11-30\",\"frequencia\":\"UNICA\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Saldo inicial registra histórico e exige data de referência")
    void saldoInicialHistorico() throws Exception {
        mockMvc.perform(post("/api/financeiro/saldo-inicial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":5000.00,\"dataReferencia\":\"2026-01-01\","
                                + "\"observacao\":\"Saldo de abertura\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(5000.00));

        mockMvc.perform(get("/api/financeiro/saldo-inicial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Saldo inicial sem data de referência retorna 400")
    void saldoInicialSemDataRetorna400() throws Exception {
        mockMvc.perform(post("/api/financeiro/saldo-inicial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":1000.00}"))
                .andExpect(status().isBadRequest());
    }
}
