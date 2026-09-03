package com.sg.auth;

import com.sg.auth.dto.LoginRequestDTO;
import com.sg.auth.dto.LoginResponseDTO;
import com.sg.shared.enums.NivelAcesso;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Testes de Autenticação")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@email.com")
                .senha("$2a$10$hashCodificado")
                .nivel(NivelAcesso.PASTOR_PRESIDENTE)
                .build();

        loginRequest = new LoginRequestDTO("joao@email.com", "123456");
    }

    @Test
    @DisplayName("Deve autenticar com sucesso e retornar token JWT")
    void deveAutenticarComSucesso() {
        when(usuarioRepository.findByEmailOrDocumento("joao@email.com", "joao@email.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("123456", usuario.getSenha())).thenReturn(true);
        when(jwtService.generateToken("joao@email.com", "PASTOR_PRESIDENTE"))
                .thenReturn("token.jwt.aqui");

        LoginResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("token.jwt.aqui", response.token());
        assertEquals("Bearer", response.tipo());
        assertEquals("João Silva", response.nome());
        assertEquals("PASTOR_PRESIDENTE", response.nivel());
        assertEquals(1L, response.idUsuario());

        verify(usuarioRepository).findByEmailOrDocumento("joao@email.com", "joao@email.com");
        verify(passwordEncoder).matches("123456", usuario.getSenha());
        verify(jwtService).generateToken("joao@email.com", "PASTOR_PRESIDENTE");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findByEmailOrDocumento(anyString(), anyString()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
        verify(usuarioRepository).findByEmailOrDocumento("joao@email.com", "joao@email.com");
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Deve lançar exceção quando senha estiver incorreta")
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        when(usuarioRepository.findByEmailOrDocumento("joao@email.com", "joao@email.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("123456", usuario.getSenha())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
        verify(passwordEncoder).matches("123456", usuario.getSenha());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Deve autenticar por documento (CPF) quando email não existir")
    void deveAutenticarPorDocumento() {
        LoginRequestDTO loginPorCpf = new LoginRequestDTO("12345678900", "123456");

        when(usuarioRepository.findByEmailOrDocumento("12345678900", "12345678900"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("123456", usuario.getSenha())).thenReturn(true);
        when(jwtService.generateToken("joao@email.com", "PASTOR_PRESIDENTE"))
                .thenReturn("token.jwt.aqui");

        LoginResponseDTO response = authService.login(loginPorCpf);

        assertNotNull(response);
        assertEquals("token.jwt.aqui", response.token());

        verify(usuarioRepository).findByEmailOrDocumento("12345678900", "12345678900");
    }
}
