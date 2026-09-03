package com.sg.eventos;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventoService - Testes de Eventos")
class EventoServiceTest {

    @Mock private EventoRepository eventoRepository;
    @InjectMocks private EventoService eventoService;

    private Evento evento;

    @BeforeEach
    void setUp() {
        evento = Evento.builder().id(1L).titulo("Culto de Domingo")
                .descricao("Culto de celebração")
                .data(LocalDate.of(2026, 8, 2))
                .hora(LocalTime.of(9, 0)).local("ICERT - Sede")
                .build();
    }

    @Test @DisplayName("Deve listar todos os eventos")
    void deveListarTodos() {
        when(eventoRepository.findAll()).thenReturn(List.of(evento));
        assertEquals(1, eventoService.listarTodos().size());
    }

    @Test @DisplayName("Deve listar próximos eventos")
    void deveListarProximos() {
        when(eventoRepository.findProximos(LocalDate.now())).thenReturn(List.of(evento));
        var result = eventoService.listarProximos();
        assertEquals(1, result.size());
        verify(eventoRepository).findProximos(LocalDate.now());
    }

    @Test @DisplayName("Deve listar eventos por mês e ano")
    void deveListarPorMesAno() {
        when(eventoRepository.findByMesAno(8, 2026)).thenReturn(List.of(evento));
        var result = eventoService.listarPorMesAno(8, 2026);
        assertEquals(1, result.size());
        verify(eventoRepository).findByMesAno(8, 2026);
    }

    @Test @DisplayName("Deve buscar evento por ID")
    void deveBuscarPorId() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        assertEquals("Culto de Domingo", eventoService.buscarPorId(1L).getTitulo());
    }

    @Test @DisplayName("Deve lançar exceção ao buscar evento inexistente")
    void deveLancarExcecaoAoBuscarInexistente() {
        when(eventoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> eventoService.buscarPorId(99L));
    }

    @Test @DisplayName("Deve salvar evento")
    void deveSalvar() {
        when(eventoRepository.save(any(Evento.class))).thenAnswer(i -> i.getArgument(0));
        assertNotNull(eventoService.salvar(evento));
    }

    @Test @DisplayName("Deve deletar evento")
    void deveDeletar() {
        when(eventoRepository.existsById(1L)).thenReturn(true);
        eventoService.deletar(1L);
        verify(eventoRepository).deleteById(1L);
    }
}
