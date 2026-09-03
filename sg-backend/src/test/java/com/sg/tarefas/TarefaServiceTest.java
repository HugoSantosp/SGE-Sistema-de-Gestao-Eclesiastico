package com.sg.tarefas;

import com.sg.shared.enums.StatusTarefa;
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
@DisplayName("TarefaService - Testes de Tarefas")
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @InjectMocks
    private TarefaService tarefaService;

    private Tarefa tarefa;

    @BeforeEach
    void setUp() {
        tarefa = Tarefa.builder()
                .id(1L).titulo("Preparar culto").descricao("Preparar som e louvor")
                .dataTarefa(LocalDate.of(2026, 8, 1))
                .horaTarefa(LocalTime.of(8, 0))
                .statusTarefa(StatusTarefa.PENDENTE)
                .build();
    }

    @Test
    @DisplayName("Deve listar todas as tarefas")
    void deveListarTodas() {
        when(tarefaRepository.findAll()).thenReturn(List.of(tarefa));

        var result = tarefaService.listarTodas();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Deve buscar tarefa por ID com sucesso")
    void deveBuscarPorId() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        Tarefa result = tarefaService.buscarPorId(1L);

        assertEquals("Preparar culto", result.getTitulo());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar tarefa inexistente")
    void deveLancarExcecaoAoBuscarInexistente() {
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Deve salvar tarefa com status PENDENTE padrão")
    void deveSalvarTarefa() {
        Tarefa novaTarefa = Tarefa.builder()
                .titulo("Reunião").dataTarefa(LocalDate.of(2026, 8, 5))
                .horaTarefa(LocalTime.of(14, 0)).build();

        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(i -> {
            Tarefa saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        Tarefa result = tarefaService.salvar(novaTarefa);

        assertEquals(2L, result.getId());
        assertEquals(StatusTarefa.PENDENTE, result.getStatusTarefa());
    }

    @Test
    @DisplayName("Deve concluir tarefa (mudar status para CONCLUIDA)")
    void deveConcluirTarefa() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(i -> i.getArgument(0));

        Tarefa result = tarefaService.concluir(1L);

        assertEquals(StatusTarefa.CONCLUIDA, result.getStatusTarefa());
    }

    @Test
    @DisplayName("Deve lançar exceção ao concluir tarefa inexistente")
    void deveLancarExcecaoAoConcluirInexistente() {
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.concluir(99L));
    }

    @Test
    @DisplayName("Deve atualizar tarefa")
    void deveAtualizarTarefa() {
        Tarefa atualizada = Tarefa.builder()
                .titulo("Preparar culto - ATUALIZADO").descricao("Som, louvor e pregação")
                .dataTarefa(LocalDate.of(2026, 8, 1))
                .horaTarefa(LocalTime.of(8, 0))
                .statusTarefa(StatusTarefa.PENDENTE).build();

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(i -> i.getArgument(0));

        Tarefa result = tarefaService.atualizar(1L, atualizada);

        assertEquals("Preparar culto - ATUALIZADO", result.getTitulo());
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Deve deletar tarefa com sucesso")
    void deveDeletarTarefa() {
        when(tarefaRepository.existsById(1L)).thenReturn(true);

        tarefaService.deletar(1L);

        verify(tarefaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar tarefa inexistente")
    void deveLancarExcecaoAoDeletarInexistente() {
        when(tarefaRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.deletar(99L));
    }
}
