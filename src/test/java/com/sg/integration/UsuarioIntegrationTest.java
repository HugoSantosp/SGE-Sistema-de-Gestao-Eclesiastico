package com.sg.integration;

import com.sg.auth.dto.LoginRequestDTO;
import com.sg.auth.dto.LoginResponseDTO;
import com.sg.shared.enums.NivelAcesso;
import com.sg.usuario.Usuario;
import com.sg.usuario.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração do módulo Usuários com PostgreSQL real.
 * 
 * Testa CRUD completo: listar, buscar, criar, atualizar, deletar.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Usuarios - Testes de Integração CRUD")
class UsuarioIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String adminToken;

    @BeforeAll
    void setupAdminToken() {
        // Cria admin para obter token
        Usuario admin = Usuario.builder()
                .nome("Admin CRUD")
                .email("admincrud@test.com")
                .senha("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy") // "123"
                .documento("11111111111")
                .nivel(NivelAcesso.PASTOR_PRESIDENTE)
                .build();
        usuarioRepository.save(admin);

        // Faz login
        LoginRequestDTO loginRequest = new LoginRequestDTO("admincrud@test.com", "123");
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
    @DisplayName("Deve listar todos os usuários")
    void deveListarUsuarios() {
        // Act
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<List<Usuario>> response = restTemplate.exchange(
                "/api/usuarios", HttpMethod.GET, entity,
                new ParameterizedTypeReference<List<Usuario>>() {});

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("Deve criar novo usuário")
    void deveCriarUsuario() {
        // Arrange
        Usuario novoUsuario = Usuario.builder()
                .nome("João Silva")
                .email("joao@test.com")
                .senha("123456")
                .documento("22222222222")
                .nivel(NivelAcesso.TESOUREIRO)
                .build();

        HttpEntity<Usuario> entity = new HttpEntity<>(novoUsuario, getAuthHeaders());

        // Act
        ResponseEntity<Usuario> response = restTemplate.postForEntity(
                "/api/usuarios", entity, Usuario.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("João Silva", response.getBody().getNome());
        assertEquals("joao@test.com", response.getBody().getEmail());
        assertEquals(NivelAcesso.TESOUREIRO, response.getBody().getNivel());
    }

    @Test
    @Order(3)
    @DisplayName("Deve buscar usuário por ID")
    void deveBuscarUsuarioPorId() {
        // Arrange - cria um usuário para buscar
        Usuario usuario = Usuario.builder()
                .nome("Maria Santos")
                .email("maria@test.com")
                .senha("123456")
                .documento("33333333333")
                .nivel(NivelAcesso.SECRETARIO)
                .build();
        Usuario salvo = usuarioRepository.save(usuario);

        // Act
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<Usuario> response = restTemplate.exchange(
                "/api/usuarios/" + salvo.getId(), HttpMethod.GET, entity, Usuario.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Maria Santos", response.getBody().getNome());
        assertEquals("maria@test.com", response.getBody().getEmail());
    }

    @Test
    @Order(4)
    @DisplayName("Deve retornar 404 ao buscar usuário inexistente")
    void deveRetornar404UsuarioInexistente() {
        // Act
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/usuarios/99999", HttpMethod.GET, entity, String.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(5)
    @DisplayName("Deve deletar usuário")
    void deveDeletarUsuario() {
        // Arrange - cria um usuário para deletar
        Usuario usuario = Usuario.builder()
                .nome("Para Deletar")
                .email("deletar@test.com")
                .senha("123456")
                .documento("44444444444")
                .nivel(NivelAcesso.MEMBRO)
                .build();
        Usuario salvo = usuarioRepository.save(usuario);

        // Act
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/usuarios/" + salvo.getId(), HttpMethod.DELETE, entity, Void.class);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(usuarioRepository.existsById(salvo.getId()));
    }
}
