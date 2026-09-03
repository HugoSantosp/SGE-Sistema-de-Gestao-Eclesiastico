package com.sg.config;

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
@DisplayName("ConfiguracaoService - Testes de Configurações")
class ConfiguracaoServiceTest {

    @Mock private ConfiguracaoRepository configuracaoRepository;
    @InjectMocks private ConfiguracaoService configuracaoService;

    private Configuracao config;

    @BeforeEach
    void setUp() {
        config = Configuracao.builder().id(1L).nome("nome_igreja")
                .valor("Igreja Sede").qtdTarefa(20).build();
    }

    @Test @DisplayName("Deve listar todas as configurações")
    void deveListarTodas() {
        when(configuracaoRepository.findAll()).thenReturn(List.of(config));
        assertEquals(1, configuracaoService.listarTodas().size());
    }

    @Test @DisplayName("Deve buscar configuração por nome")
    void deveBuscarPorNome() {
        when(configuracaoRepository.findByNome("nome_igreja")).thenReturn(Optional.of(config));
        assertEquals("Igreja Sede", configuracaoService.buscarPorNome("nome_igreja").getValor());
    }

    @Test @DisplayName("Deve lançar exceção ao buscar configuração por nome inexistente")
    void deveLancarExcecaoAoBuscarPorNomeInexistente() {
        when(configuracaoRepository.findByNome("inexistente")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> configuracaoService.buscarPorNome("inexistente"));
    }

    @Test @DisplayName("Deve salvar configuração")
    void deveSalvar() {
        when(configuracaoRepository.save(any(Configuracao.class))).thenAnswer(i -> i.getArgument(0));
        assertNotNull(configuracaoService.salvar(config));
    }

    @Test @DisplayName("Deve deletar configuração")
    void deveDeletar() {
        when(configuracaoRepository.existsById(1L)).thenReturn(true);
        configuracaoService.deletar(1L);
        verify(configuracaoRepository).deleteById(1L);
    }

    @Test @DisplayName("Deve lançar exceção ao deletar configuração inexistente")
    void deveLancarExcecaoAoDeletarInexistente() {
        when(configuracaoRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> configuracaoService.deletar(99L));
    }
}
