package com.sg.presbiteros;

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
@DisplayName("PresbiteroService - Testes de CRUD")
class PresbiteroServiceTest {

    @Mock private PresbiteroRepository presbiteroRepository;
    @InjectMocks private PresbiteroService presbiteroService;

    private Presbitero presbitero;

    @BeforeEach
    void setUp() {
        presbitero = Presbitero.builder().id(1L).nome("Pb. Carlos").email("carlos@email.com")
                .dataCad(LocalDate.now()).build();
    }

    @Test void deveListarTodos() {
        when(presbiteroRepository.findAll()).thenReturn(List.of(presbitero));
        assertEquals(1, presbiteroService.listarTodos().size());
    }

    @Test void deveBuscarPorId() {
        when(presbiteroRepository.findById(1L)).thenReturn(Optional.of(presbitero));
        assertEquals("Pb. Carlos", presbiteroService.buscarPorId(1L).getNome());
    }

    @Test void deveLancarExcecaoAoBuscarIdInexistente() {
        when(presbiteroRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> presbiteroService.buscarPorId(99L));
    }

    @Test void deveSalvarComDataCadAutomatica() {
        Presbitero novo = Presbitero.builder().nome("Pb. João").email("joao@email.com").build();
        when(presbiteroRepository.save(any(Presbitero.class))).thenAnswer(i -> {
            Presbitero saved = i.getArgument(0); saved.setId(2L); return saved;
        });
        Presbitero result = presbiteroService.salvar(novo);
        assertEquals(2L, result.getId());
        assertEquals(LocalDate.now(), result.getDataCad());
    }

    @Test void deveAtualizar() {
        Presbitero atualizado = Presbitero.builder().nome("Pb. Carlos Silva").email("carlos@email.com").build();
        when(presbiteroRepository.findById(1L)).thenReturn(Optional.of(presbitero));
        when(presbiteroRepository.save(any(Presbitero.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals("Pb. Carlos Silva", presbiteroService.atualizar(1L, atualizado).getNome());
    }

    @Test void deveDeletar() {
        when(presbiteroRepository.existsById(1L)).thenReturn(true);
        presbiteroService.deletar(1L);
        verify(presbiteroRepository).deleteById(1L);
    }
}
