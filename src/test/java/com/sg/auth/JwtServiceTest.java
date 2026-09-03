package com.sg.auth;

import com.sg.shared.exceptions.InvalidCredentialsException;
import com.sg.usuario.Usuario;
import com.sg.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService - Testes de Token JWT")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "58656c6c6f576f726c64546869734973416e4578616d706c654a7774536563726574466f7254657374696e67507572706f736573";
    private static final long EXPIRATION = 3600000; // 1 hora

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido")
    void deveGerarTokenValido() {
        String token = jwtService.generateToken("joao@email.com", "PASTOR_PRESIDENTE");

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "Token JWT deve ter 3 partes");
    }

    @Test
    @DisplayName("Deve extrair email do token")
    void deveExtrairEmail() {
        String token = jwtService.generateToken("joao@email.com", "PASTOR_PRESIDENTE");

        String email = jwtService.extractEmail(token);

        assertEquals("joao@email.com", email);
    }

    @Test
    @DisplayName("Deve extrair nível de acesso do token")
    void deveExtrairNivel() {
        String token = jwtService.generateToken("joao@email.com", "TESOUREIRO");

        String nivel = jwtService.extractNivel(token);

        assertEquals("TESOUREIRO", nivel);
    }

    @Test
    @DisplayName("Deve validar token válido como true")
    void deveValidarTokenValido() {
        String token = jwtService.generateToken("joao@email.com", "PASTOR_PRESIDENTE");

        assertTrue(jwtService.isValid(token));
    }

    @Test
    @DisplayName("Deve retornar false para token inválido")
    void deveRejeitarTokenInvalido() {
        assertFalse(jwtService.isValid("token.invalido.aqui"));
    }

    @Test
    @DisplayName("Deve retornar false para token vazio")
    void deveRejeitarTokenVazio() {
        assertFalse(jwtService.isValid(""));
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para emails diferentes")
    void deveGerarTokensDiferentes() {
        String token1 = jwtService.generateToken("joao@email.com", "PASTOR_PRESIDENTE");
        String token2 = jwtService.generateToken("maria@email.com", "SECRETARIO");

        assertNotEquals(token1, token2);
    }
}
