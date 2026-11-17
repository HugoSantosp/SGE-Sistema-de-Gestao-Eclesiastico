package com.sg.celulas;

import com.sg.membros.Membro;
import com.sg.membros.MembroRepository;
import com.sg.shared.enums.StatusMembro;
import com.sg.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CelulaService - Testes de Células")
class CelulaServiceTest {

    @Mock private CelulaRepository celulaRepository;
    @Mock private MembroRepository membroRepository;
    @InjectMocks private CelulaService celulaService;

    private Celula celula;

    @BeforeEach
    void setUp() {
        celula = Celula.builder().id(1L).nome("Célula do Centro")
                .lider("Pr. João").endereco("Rua A, 150")
                .diaSemana("Terça-feira").horario(LocalTime.of(19, 30))
                .descricao("Estudo bíblico").build();
    }

    @Test @DisplayName("Deve listar todas as células")
    void deveListarTodos() {
        when(celulaRepository.findAll()).thenReturn(List.of(celula));
        assertEquals(1, celulaService.listarTodos().size());
    }

    @Test @DisplayName("Deve listar células públicas")
    void deveListarPublicas() {
        when(celulaRepository.findAll()).thenReturn(List.of(celula));
        assertEquals(1, celulaService.listarPublicas().size());
    }

    @Test @DisplayName("Deve buscar célula por ID")
    void deveBuscarPorId() {
        when(celulaRepository.findById(1L)).thenReturn(Optional.of(celula));
        assertEquals("Célula do Centro", celulaService.buscarPorId(1L).getNome());
    }

    @Test @DisplayName("Deve lançar exceção ao buscar célula inexistente")
    void deveLancarExcecaoAoBuscarInexistente() {
        when(celulaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> celulaService.buscarPorId(99L));
    }

    @Test @DisplayName("Deve salvar célula")
    void deveSalvar() {
        when(celulaRepository.save(any(Celula.class))).thenAnswer(i -> i.getArgument(0));
        assertNotNull(celulaService.salvar(celula));
    }

    @Test @DisplayName("Deve deletar célula")
    void deveDeletar() {
        when(celulaRepository.existsById(1L)).thenReturn(true);
        celulaService.deletar(1L);
        verify(celulaRepository).deleteById(1L);
    }

    @Test @DisplayName("Sem líder selecionado mantém o texto atual e não consulta membros")
    void semLiderMantemTextoAtual() {
        when(celulaRepository.save(any(Celula.class))).thenAnswer(i -> i.getArgument(0));

        Celula salva = celulaService.salvar(celula);

        assertEquals("Pr. João", salva.getLider());
        assertNull(salva.getLiderId());
        verifyNoInteractions(membroRepository);
    }

    @Test @DisplayName("Líder válido sincroniza a coluna de texto com o nome do membro")
    void liderValidoSincronizaNome() {
        celula.setLiderId(7L);
        Membro membro = Membro.builder()
                .id(7L).nome("Diác. Paulo Santos").situacao(StatusMembro.ATIVO).build();
        when(membroRepository.findById(7L)).thenReturn(Optional.of(membro));
        when(celulaRepository.save(any(Celula.class))).thenAnswer(i -> i.getArgument(0));

        Celula salva = celulaService.salvar(celula);

        assertEquals(7L, salva.getLiderId());
        assertEquals("Diác. Paulo Santos", salva.getLider());
    }

    @Test @DisplayName("Líder inexistente gera 404 e não salva a célula")
    void liderInexistenteNaoSalva() {
        celula.setLiderId(99L);
        when(membroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> celulaService.salvar(celula));
        verify(celulaRepository, never()).save(any(Celula.class));
    }
}
