package com.sg.integration;

import com.sg.auth.dto.LoginRequestDTO;
import com.sg.auth.dto.LoginResponseDTO;
import com.sg.shared.enums.NivelAcesso;
import com.sg.usuario.Usuario;
import com.sg.usuario.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração do módulo Auth com PostgreSQL real.
 * 
 * Testa fluxo completo: criação de usuário → login → obtenção de token.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Auth - Testes de Integração")
class AuthIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static boolean initialized = false;

    @BeforeEach
    void setUp() {
        if (!initialized) {
            // Cria um usuário de teste para autenticação
            Usuario usuario = Usuario.builder()
                    .nome("Admin Teste")
                    .email("admin@test.com")
                    .senha("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy") // senha: "123"
                    .documento("12345678901")
                    .nivel(NivelAcesso.PASTOR_PRESIDENTE)
                    .build();
            usuarioRepository.save(usuario);
            initialized = true;
        }
    }

    @Test
    @Order(1)
    @DisplayName("Deve autenticar com email e senha válidos")
    void deveAutenticarComEmail() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("admin@test.com", "123");

        // Act
        ResponseEntity<LoginResponseDTO> response = restTemplate.postForEntity(
                "/auth/login", request, LoginResponseDTO.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().token());
        assertEquals("Admin Teste", response.getBody().nome());
        assertEquals("PASTOR_PRESIDENTE", response.getBody().nivel());
        assertFalse(response.getBody().senhaTemporaria());
    }

    @Test
    @Order(2)
    @DisplayName("Deve autenticar com documento (CPF)")
    void deveAutenticarComDocumento() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("12345678901", "123");

        // Act
        ResponseEntity<LoginResponseDTO> response = restTemplate.postForEntity(
                "/auth/login", request, LoginResponseDTO.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().token());
    }

    @Test
    @Order(3)
    @DisplayName("Deve retornar 401 quando senha incorreta")
    void deveRetornar401SenhaIncorreta() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("admin@test.com", "senhaerrada");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", request, String.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(4)
    @DisplayName("Deve retornar 401 quando usuário não existe")
    void deveRetornar401UsuarioNaoExiste() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("naoexiste@test.com", "123");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", request, String.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(5)
    @DisplayName("Deve acessar endpoint protegido com token válido")
    void deveAcessarEndpointProtegidoComToken() {
        // Arrange - faz login para obter token
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin@test.com", "123");
        ResponseEntity<LoginResponseDTO> loginResponse = restTemplate.postForEntity(
                "/auth/login", loginRequest, LoginResponseDTO.class);
        String token = loginResponse.getBody().token();

        // Act - acessa endpoint protegido
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/usuarios", HttpMethod.GET, entity, String.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(6)
    @DisplayName("Deve retornar 403/401 sem token em endpoint protegido")
    void deveRetornar401SemToken() {
        // Act - acessa endpoint protegido sem token
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/usuarios", String.class);

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
    }
}
