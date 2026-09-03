package com.sg.secretarios;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecretarioService - Testes de CRUD")
class SecretarioServiceTest {

    @Mock private SecretarioRepository secretarioRepository;
    @InjectMocks private SecretarioService secretarioService;

    private Secretario secretario;

    @BeforeEach
    void setUp() {
        secretario = Secretario.builder().id(1L).nome("Sec. Maria").email("maria@email.com")
                .dataCad(LocalDate.now()).build();
    }

    @Test void deveListarTodos() {
        when(secretarioRepository.findAll()).thenReturn(List.of(secretario));
        assertEquals(1, secretarioService.listarTodos().size());
    }

    @Test void deveBuscarPorId() {
        when(secretarioRepository.findById(1L)).thenReturn(Optional.of(secretario));
        assertEquals("Sec. Maria", secretarioService.buscarPorId(1L).getNome());
    }

    @Test void deveLancarExcecaoAoBuscarIdInexistente() {
        when(secretarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> secretarioService.buscarPorId(99L));
    }

    @Test void deveSalvarComDataCadAutomatica() {
        Secretario novo = Secretario.builder().nome("Sec. Ana").email("ana@email.com").build();
        when(secretarioRepository.save(any(Secretario.class))).thenAnswer(i -> {
            Secretario saved = i.getArgument(0); saved.setId(2L); return saved;
        });
        Secretario result = secretarioService.salvar(novo);
        assertEquals(2L, result.getId());
        assertNotNull(result.getDataCad());
    }

    @Test void deveAtualizar() {
        Secretario atualizado = Secretario.builder().nome("Sec. Maria Silva").email("maria@email.com").build();
        when(secretarioRepository.findById(1L)).thenReturn(Optional.of(secretario));
        when(secretarioRepository.save(any(Secretario.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals("Sec. Maria Silva", secretarioService.atualizar(1L, atualizado).getNome());
    }

    @Test void deveDeletar() {
        when(secretarioRepository.existsById(1L)).thenReturn(true);
        secretarioService.deletar(1L);
        verify(secretarioRepository).deleteById(1L);
    }
}
