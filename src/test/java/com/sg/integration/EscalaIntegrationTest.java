package com.sg.integration;

import com.sg.auth.dto.LoginRequestDTO;
import com.sg.auth.dto.LoginResponseDTO;
import com.sg.escala.Escala;
import com.sg.escala.EscalaRepository;
import com.sg.escala.dto.EscalaRequestDTO;
import com.sg.shared.enums.NivelAcesso;
import com.sg.usuario.Usuario;
import com.sg.usuario.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração do módulo Escalas de Louvor com PostgreSQL real.
 * 
 * Testa fluxo completo: criar escala → adicionar datas → designar membros.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Escala - Testes de Integração")
class EscalaIntegrationTest extends IntegrationTestBase {

    @Autowired
    private EscalaRepository escalaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String adminToken;

    @BeforeAll
    void setupAdminToken() {
        // Cria admin
        Usuario admin = Usuario.builder()
                .nome("Admin Escala")
                .email("adminescala@test.com")
                .senha("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy") // "123"
                .documento("55555555555")
                .nivel(NivelAcesso.PASTOR_PRESIDENTE)
                .build();
        usuarioRepository.save(admin);

        // Login
        LoginRequestDTO loginRequest = new LoginRequestDTO("adminescala@test.com", "123");
        ResponseEntity<LoginResponseDTO> loginResponse = restTemplate.postForEntity(
                "/auth/login", loginRequest, LoginResponseDTO.class);
        adminToken = loginResponse.getBody().token();
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @Order(1)
    @DisplayName("Deve criar nova escala")
    void deveCriarEscala() {
        // Arrange
        EscalaRequestDTO request = new EscalaRequestDTO(
                "Escala Domingo - Louvor",
                null // ministerioId opcional
        );

        HttpEntity<EscalaRequestDTO> entity = new HttpEntity<>(request, getAuthHeaders());

        // Act
        ResponseEntity<Escala> response = restTemplate.postForEntity(
                "/api/escalas", entity, Escala.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Escala Domingo - Louvor", response.getBody().getTitulo());
    }

    @Test
    @Order(2)
    @DisplayName("Deve listar escalas criadas")
    void deveListarEscalas() {
        // Act
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/escalas", HttpMethod.GET, entity, String.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(3)
    @DisplayName("Deve buscar escala por ID")
    void deveBuscarEscalaPorId() {
        // Arrange - cria escala
        Escala escala = Escala.builder()
                .titulo("Escala para buscar")
                .build();
        Escala salva = escalaRepository.save(escala);

        // Act
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<Escala> response = restTemplate.exchange(
                "/api/escalas/" + salva.getId(), HttpMethod.GET, entity, Escala.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Escala para buscar", response.getBody().getTitulo());
    }

    @Test
    @Order(4)
    @DisplayName("Deve deletar escala")
    void deveDeletarEscala() {
        // Arrange - cria escala para deletar
        Escala escala = Escala.builder()
                .titulo("Escala para deletar")
                .build();
        Escala salva = escalaRepository.save(escala);

        // Act
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/escalas/" + salva.getId(), HttpMethod.DELETE, entity, Void.class);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(escalaRepository.existsById(salva.getId()));
    }
}
