package com.sg.ministerios;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MinisterioService - Testes de Ministérios")
class MinisterioServiceTest {

    @Mock private MinisterioRepository ministerioRepository;
    @InjectMocks private MinisterioService ministerioService;

    private Ministerio ministerio;

    @BeforeEach
    void setUp() {
        ministerio = Ministerio.builder().id(1L).nome("Louvor")
                .descricao("Ministério de música").build();
    }

    @Test @DisplayName("Deve listar todos os ministérios")
    void deveListarTodos() {
        when(ministerioRepository.findAll()).thenReturn(List.of(ministerio));
        assertEquals(1, ministerioService.listarTodos().size());
    }

    @Test @DisplayName("Deve buscar ministério por ID")
    void deveBuscarPorId() {
        when(ministerioRepository.findById(1L)).thenReturn(Optional.of(ministerio));
        assertEquals("Louvor", ministerioService.buscarPorId(1L).getNome());
    }

    @Test @DisplayName("Deve lançar exceção ao buscar ministério inexistente")
    void deveLancarExcecaoAoBuscarInexistente() {
        when(ministerioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> ministerioService.buscarPorId(99L));
    }

    @Test @DisplayName("Deve salvar ministério")
    void deveSalvar() {
        when(ministerioRepository.save(any(Ministerio.class))).thenAnswer(i -> i.getArgument(0));
        assertNotNull(ministerioService.salvar(ministerio));
    }

    @Test @DisplayName("Deve deletar ministério")
    void deveDeletar() {
        when(ministerioRepository.existsById(1L)).thenReturn(true);
        ministerioService.deletar(1L);
        verify(ministerioRepository).deleteById(1L);
    }

    @Test @DisplayName("Deve lançar exceção ao deletar ministério inexistente")
    void deveLancarExcecaoAoDeletarInexistente() {
        when(ministerioRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> ministerioService.deletar(99L));
    }
}
