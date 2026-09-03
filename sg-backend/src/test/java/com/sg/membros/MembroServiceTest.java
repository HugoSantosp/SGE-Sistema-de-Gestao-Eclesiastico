package com.sg.membros;

import com.sg.shared.enums.StatusMembro;
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
@DisplayName("MembroService - Testes de Membros")
class MembroServiceTest {

    @Mock
    private MembroRepository membroRepository;

    @InjectMocks
    private MembroService membroService;

    private Membro membroAtivo;
    private Membro membroInativo;

    @BeforeEach
    void setUp() {
        membroAtivo = Membro.builder()
                .id(1L).nome("Carlos").situacao(StatusMembro.ATIVO)
                .build();

        membroInativo = Membro.builder()
                .id(2L).nome("Ana").situacao(StatusMembro.INATIVO)
                .build();
    }

    @Test
    @DisplayName("Deve listar todos os membros")
    void deveListarTodos() {
        when(membroRepository.findAll()).thenReturn(List.of(membroAtivo, membroInativo));

        var result = membroService.listarTodos();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve buscar membro por ID")
    void deveBuscarPorId() {
        when(membroRepository.findById(1L)).thenReturn(Optional.of(membroAtivo));

        Membro result = membroService.buscarPorId(1L);

        assertEquals("Carlos", result.getNome());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar membro inexistente")
    void deveLancarExcecaoAoBuscarInexistente() {
        when(membroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> membroService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Deve criar membro com dataCad automática")
    void deveCriarMembro() {
        Membro novoMembro = Membro.builder()
                .nome("Pedro").situacao(StatusMembro.ATIVO).build();

        when(membroRepository.save(any(Membro.class))).thenAnswer(i -> {
            Membro saved = i.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        Membro result = membroService.salvar(novoMembro);

        assertEquals(3L, result.getId());
        assertEquals(LocalDate.now(), result.getDataCad());
    }

    @Test
    @DisplayName("Deve contar total de membros")
    void deveContarTotal() {
        when(membroRepository.count()).thenReturn(2L);

        long total = membroService.contarTotal();

        assertEquals(2L, total);
    }

    @Test
    @DisplayName("Deve contar membros ativos")
    void deveContarAtivos() {
        when(membroRepository.countBySituacao(StatusMembro.ATIVO)).thenReturn(1L);

        long ativos = membroService.contarAtivos();

        assertEquals(1L, ativos);
    }

    @Test
    @DisplayName("Deve contar membros inativos")
    void deveContarInativos() {
        when(membroRepository.countBySituacao(StatusMembro.INATIVO)).thenReturn(1L);

        long inativos = membroService.contarInativos();

        assertEquals(1L, inativos);
    }

    @Test
    @DisplayName("Deve atualizar membro")
    void deveAtualizarMembro() {
        Membro atualizado = Membro.builder().nome("Carlos Silva").situacao(StatusMembro.ATIVO).build();

        when(membroRepository.findById(1L)).thenReturn(Optional.of(membroAtivo));
        when(membroRepository.save(any(Membro.class))).thenAnswer(i -> i.getArgument(0));

        Membro result = membroService.atualizar(1L, atualizado);

        assertEquals("Carlos Silva", result.getNome());
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Deve deletar membro")
    void deveDeletarMembro() {
        when(membroRepository.existsById(1L)).thenReturn(true);

        membroService.deletar(1L);

        verify(membroRepository).deleteById(1L);
    }
}
