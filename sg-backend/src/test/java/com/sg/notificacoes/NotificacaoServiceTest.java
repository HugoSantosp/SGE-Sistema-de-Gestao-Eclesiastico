package com.sg.notificacoes;

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
@DisplayName("NotificacaoService - Testes de Notificações")
class NotificacaoServiceTest {

    @Mock private NotificacaoRepository notificacaoRepository;
    @InjectMocks private NotificacaoService notificacaoService;

    private Notificacao notificacao;

    @BeforeEach
    void setUp() {
        notificacao = Notificacao.builder().id(1L).nome("Culto de Domingo")
                .atividade("Culto").hora(LocalTime.of(9, 0))
                .dataNot(LocalDate.now()).statusNot("PENDENTE").build();
    }

    @Test @DisplayName("Deve listar todas as notificações")
    void deveListarTodas() {
        when(notificacaoRepository.findAll()).thenReturn(List.of(notificacao));
        assertEquals(1, notificacaoService.listarTodas().size());
    }

    @Test @DisplayName("Deve listar notificações do dia")
    void deveListarDoDia() {
        when(notificacaoRepository.buscarPorData(LocalDate.now())).thenReturn(List.of(notificacao));
        var result = notificacaoService.listarDoDia();
        assertEquals(1, result.size());
        verify(notificacaoRepository).buscarPorData(LocalDate.now());
    }

    @Test @DisplayName("Deve buscar notificação por ID")
    void deveBuscarPorId() {
        when(notificacaoRepository.findById(1L)).thenReturn(Optional.of(notificacao));
        assertEquals("Culto de Domingo", notificacaoService.buscarPorId(1L).getNome());
    }

    @Test @DisplayName("Deve lançar exceção ao buscar ID inexistente")
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(notificacaoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> notificacaoService.buscarPorId(99L));
    }

    @Test @DisplayName("Deve salvar notificação")
    void deveSalvar() {
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(i -> i.getArgument(0));
        assertNotNull(notificacaoService.salvar(notificacao));
    }

    @Test @DisplayName("Deve deletar notificação")
    void deveDeletar() {
        when(notificacaoRepository.existsById(1L)).thenReturn(true);
        notificacaoService.deletar(1L);
        verify(notificacaoRepository).deleteById(1L);
    }

    @Test @DisplayName("Deve lançar exceção ao deletar notificação inexistente")
    void deveLancarExcecaoAoDeletarInexistente() {
        when(notificacaoRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> notificacaoService.deletar(99L));
    }
}
