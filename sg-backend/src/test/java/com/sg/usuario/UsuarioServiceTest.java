package com.sg.usuario;

import com.sg.bispos.Bispo;
import com.sg.bispos.BispoRepository;
import com.sg.membros.Membro;
import com.sg.membros.MembroRepository;
import com.sg.ministerios.MinisterioMembroRepository;
import com.sg.presbiteros.Presbitero;
import com.sg.presbiteros.PresbiteroRepository;
import com.sg.secretarios.Secretario;
import com.sg.secretarios.SecretarioRepository;
import com.sg.shared.enums.NivelAcesso;
import com.sg.shared.exceptions.ResourceNotFoundException;
import com.sg.tesoureiros.Tesoureiro;
import com.sg.tesoureiros.TesoureiroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Testes de Usuário e Sincronização de Cargos")
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TesoureiroRepository tesoureiroRepository;
    @Mock private PresbiteroRepository presbiteroRepository;
    @Mock private SecretarioRepository secretarioRepository;
    @Mock private BispoRepository bispoRepository;
    @Mock private MembroRepository membroRepository;
    @Mock private MinisterioMembroRepository ministerioMembroRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Captor
    private ArgumentCaptor<Usuario> usuarioCaptor;

    private Usuario usuario;
    private Tesoureiro tesoureiro;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@email.com")
                .senha("senha123")
                .documento("12345678900")
                .nivel(NivelAcesso.TESOUREIRO)
                .build();

        tesoureiro = Tesoureiro.builder()
                .id(10L)
                .nome("João Silva")
                .email("joao@email.com")
                .documento("12345678900")
                .dataCad(LocalDate.now())
                .build();
    }

    // ===== loadUserByUsername =====

    @Test
    @DisplayName("Deve carregar usuário por email para autenticação")
    void deveCarregarUsuarioPorEmail() {
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioService.loadUserByUsername("joao@email.com");

        assertNotNull(userDetails);
        assertEquals("joao@email.com", userDetails.getUsername());
        assertEquals("senha123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TESOUREIRO")));
    }

    @Test
    @DisplayName("Deve lançar exceção quando email não encontrado no loadUserByUsername")
    void deveLancarExcecaoQuandoEmailNaoEncontrado() {
        when(usuarioRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> usuarioService.loadUserByUsername("naoexiste@email.com"));
    }

    // ===== listarTodos =====

    @Test
    @DisplayName("Deve listar todos os usuários")
    void deveListarTodosUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<Usuario> result = usuarioService.listarTodos();

        assertEquals(1, result.size());
        verify(usuarioRepository).findAll();
    }

    // ===== buscarPorId =====

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void deveBuscarUsuarioPorId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario result = usuarioService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals("João Silva", result.getNome());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar usuário por ID inexistente")
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.buscarPorId(99L));
    }

    // ===== salvar (criar) =====

    @Test
    @DisplayName("Deve criar usuário e sincronizar com Tesoureiro automaticamente")
    void deveCriarUsuarioComSincronizacaoTesoureiro() {
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashCodificado");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(tesoureiroRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(tesoureiroRepository.save(any(Tesoureiro.class))).thenReturn(tesoureiro);

        Usuario result = usuarioService.salvar(usuario);

        assertNotNull(result);

        // Verifica que a senha foi codificada (2 saves: criação + vínculo do idPessoa)
        verify(passwordEncoder).encode("senha123");
        verify(usuarioRepository, times(2)).save(usuarioCaptor.capture());
        assertEquals("$2a$10$hashCodificado", usuarioCaptor.getAllValues().get(0).getSenha());

        // Verifica sincronização com Tesoureiro
        verify(tesoureiroRepository).findByEmail("joao@email.com");
        verify(tesoureiroRepository).save(any(Tesoureiro.class));

        // Verifica que o idPessoa foi vinculado ao usuário na 2ª atualização
        assertEquals(10L, usuarioCaptor.getAllValues().get(1).getIdPessoa());
    }

    @Test
    @DisplayName("Deve criar usuário e sincronizar com Presbitero automaticamente")
    void deveCriarUsuarioComSincronizacaoPresbitero() {
        usuario.setNivel(NivelAcesso.PASTOR_AUXILIAR);
        Presbitero presbitero = Presbitero.builder().id(20L).nome("João Silva").email("joao@email.com").build();

        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashCodificado");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(presbiteroRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(presbiteroRepository.save(any(Presbitero.class))).thenReturn(presbitero);

        usuarioService.salvar(usuario);

        verify(presbiteroRepository).findByEmail("joao@email.com");
        verify(presbiteroRepository).save(any(Presbitero.class));
    }

    @Test
    @DisplayName("Deve criar usuário e sincronizar com Secretario automaticamente")
    void deveCriarUsuarioComSincronizacaoSecretario() {
        usuario.setNivel(NivelAcesso.SECRETARIO);
        Secretario secretario = Secretario.builder().id(30L).nome("João Silva").email("joao@email.com").build();

        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashCodificado");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(secretarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(secretarioRepository.save(any(Secretario.class))).thenReturn(secretario);

        usuarioService.salvar(usuario);

        verify(secretarioRepository).findByEmail("joao@email.com");
        verify(secretarioRepository).save(any(Secretario.class));
    }

    @Test
    @DisplayName("Deve criar usuário e sincronizar com Bispo automaticamente")
    void deveCriarUsuarioComSincronizacaoBispo() {
        usuario.setNivel(NivelAcesso.PASTOR_PRESIDENTE);
        Bispo bispo = Bispo.builder().id(40L).nome("João Silva").email("joao@email.com").build();

        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashCodificado");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(bispoRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(bispoRepository.save(any(Bispo.class))).thenReturn(bispo);

        usuarioService.salvar(usuario);

        verify(bispoRepository).findByEmail("joao@email.com");
        verify(bispoRepository).save(any(Bispo.class));
    }

    @Test
    @DisplayName("Deve criar usuário e sincronizar com a tabela de membros (papel MEMBRO)")
    void deveCriarUsuarioComSincronizacaoMembro() {
        usuario.setNivel(NivelAcesso.MEMBRO);
        Membro membro = Membro.builder()
                .id(50L).nome("João Silva").documento("12345678900").build();

        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashCodificado");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(membroRepository.findByDocumento("12345678900")).thenReturn(Optional.empty());
        when(membroRepository.save(any(Membro.class))).thenReturn(membro);

        usuarioService.salvar(usuario);

        verify(membroRepository).findByDocumento("12345678900");
        verify(membroRepository).save(any(Membro.class));
        // O idPessoa do usuário é vinculado ao Membro criado
        assertEquals(50L, usuario.getIdPessoa());
    }

    @Test
    @DisplayName("Não deve recodificar senha se já estiver em formato BCrypt")
    void naoDeveRecodificarSenhaBCrypt() {
        usuario.setSenha("$2a$10$hashExistenteValido1234567890123456789012345678901234567890");

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(tesoureiroRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(tesoureiroRepository.save(any(Tesoureiro.class))).thenReturn(tesoureiro);

        usuarioService.salvar(usuario);

        verify(passwordEncoder, never()).encode(anyString());
    }

    // ===== salvar (atualizar com mudança de role) =====

    @Test
    @DisplayName("Deve remover cargo antigo e criar novo quando role mudar")
    void deveRemoverCargoAntigoECriarNovoQuandoRoleMudar() {
        usuario.setNivel(NivelAcesso.TESOUREIRO);
        // Simula que o usuário já existe com role SECRETARIO
        Usuario usuarioExistente = Usuario.builder().id(1L).email("joao@email.com").nivel(NivelAcesso.SECRETARIO).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(secretarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(new Secretario()));
        when(tesoureiroRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(tesoureiroRepository.save(any(Tesoureiro.class))).thenReturn(tesoureiro);

        usuarioService.salvar(usuario);

        // Verifica que removeu o Secretario antigo
        verify(secretarioRepository).findByEmail("joao@email.com");
        verify(secretarioRepository).delete(any(Secretario.class));

        // Verifica que criou o Tesoureiro novo
        verify(tesoureiroRepository).save(any(Tesoureiro.class));
    }

    // ===== deletar =====

    @Test
    @DisplayName("Deve deletar usuário e remover cargo sincronizado")
    void deveDeletarUsuarioComRemocaoDeCargo() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(tesoureiroRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(tesoureiro));

        usuarioService.deletar(1L);

        verify(usuarioRepository).findById(1L);
        verify(tesoureiroRepository).findByEmail("joao@email.com");
        verify(tesoureiroRepository).delete(tesoureiro);
        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar usuário inexistente")
    void deveLancarExcecaoAoDeletarUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.deletar(99L));
        verify(usuarioRepository, never()).deleteById(anyLong());
    }
}
