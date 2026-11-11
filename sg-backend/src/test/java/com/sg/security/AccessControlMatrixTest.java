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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica a matriz de níveis de acesso aprovada em MATRIZ_NIVEIS_ACESSO.md.
 *
 * Sobe o contexto completo com o perfil "test" (H2 em memória) e usa
 * {@link WithMockUser} para simular cada perfil. O JwtAuthenticationFilter
 * ignora requisições sem cabeçalho Authorization, então o usuário mockado
 * permanece no SecurityContext e as regras de @PreAuthorize são exercitadas
 * de verdade, incluindo a tradução de AccessDeniedException em HTTP 403.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccessControlMatrixTest {

    @Autowired
    private MockMvc mockMvc;

    // ===== Sem autenticação =====

    @Test
    @DisplayName("Sem token: qualquer endpoint protegido é rejeitado")
    void semAutenticacaoEhRejeitado() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().is4xxClientError());
    }

    // ===== /api/usuarios =====

    @Test
    @WithMockUser(roles = "PASTOR_PRESIDENTE")
    @DisplayName("Presidente lista usuários")
    void presidenteListaUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PASTOR_AUXILIAR")
    @DisplayName("Auxiliar lista usuários")
    void auxiliarListaUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SECRETARIO")
    @DisplayName("Secretário não lista usuários")
    void secretarioNaoListaUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Tesoureiro não lista usuários")
    void tesoureiroNaoListaUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    @DisplayName("Membro não lista usuários")
    void membroNaoListaUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASTOR_AUXILIAR")
    @DisplayName("Auxiliar cria usuário")
    void auxiliarCriaUsuario() throws Exception {
        String body = """
                {
                  "nome": "Teste Auxiliar",
                  "email": "auxiliar.teste@icert.local",
                  "senha": "senha123",
                  "nivel": "SECRETARIO"
                }
                """;
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    // ===== /api/financeiro =====

    @Test
    @WithMockUser(roles = "PASTOR_AUXILIAR")
    @DisplayName("Auxiliar lê contas a pagar")
    void auxiliarLeFinanceiro() throws Exception {
        mockMvc.perform(get("/api/financeiro/contas-pagar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PASTOR_AUXILIAR")
    @DisplayName("Auxiliar não cria conta a pagar")
    void auxiliarNaoCriaContaPagar() throws Exception {
        mockMvc.perform(post("/api/financeiro/contas-pagar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contaPagarJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Tesoureiro cria conta a pagar")
    void tesoureiroCriaContaPagar() throws Exception {
        mockMvc.perform(post("/api/financeiro/contas-pagar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contaPagarJson()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "SECRETARIO")
    @DisplayName("Secretário não acessa o financeiro")
    void secretarioNaoAcessaFinanceiro() throws Exception {
        mockMvc.perform(get("/api/financeiro/contas-pagar"))
                .andExpect(status().isForbidden());
    }

    // ===== /api/config =====

    @Test
    @WithMockUser(roles = "SECRETARIO")
    @DisplayName("Secretário lê configurações")
    void secretarioLeConfiguracoes() throws Exception {
        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SECRETARIO")
    @DisplayName("Secretário não escreve configurações")
    void secretarioNaoEscreveConfiguracoes() throws Exception {
        mockMvc.perform(post("/api/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"teste.secretario\",\"valor\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASTOR_AUXILIAR")
    @DisplayName("Auxiliar escreve configurações")
    void auxiliarEscreveConfiguracoes() throws Exception {
        mockMvc.perform(post("/api/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"teste.auxiliar\",\"valor\":\"x\"}"))
                .andExpect(status().isCreated());
    }

    // ===== /api/membros =====

    @Test
    @WithMockUser(roles = "MEMBRO")
    @DisplayName("Membro lê membros (Meu Ministério)")
    void membroLeMembros() throws Exception {
        mockMvc.perform(get("/api/membros"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Tesoureiro não lê membros")
    void tesoureiroNaoLeMembros() throws Exception {
        mockMvc.perform(get("/api/membros"))
                .andExpect(status().isForbidden());
    }

    // ===== /api/eventos =====

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Tesoureiro lê eventos")
    void tesoureiroLeEventos() throws Exception {
        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TESOUREIRO")
    @DisplayName("Tesoureiro não cria evento")
    void tesoureiroNaoCriaEvento() throws Exception {
        mockMvc.perform(post("/api/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Teste\",\"data\":\"2026-12-01\"}"))
                .andExpect(status().isForbidden());
    }

    private String contaPagarJson() {
        return """
                {
                  "descricao": "Conta de teste",
                  "valor": 100.00,
                  "vencimento": "2026-12-01",
                  "frequencia": "MENSAL"
                }
                """;
    }
}
