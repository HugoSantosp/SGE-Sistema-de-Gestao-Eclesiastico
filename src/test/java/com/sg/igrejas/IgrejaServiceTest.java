package com.sg.igrejas;

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
@DisplayName("IgrejaService - Testes de Igrejas")
class IgrejaServiceTest {

    @Mock private IgrejaRepository igrejaRepository;
    @InjectMocks private IgrejaService igrejaService;

    private Igreja igreja;

    @BeforeEach
    void setUp() {
        igreja = Igreja.builder().id(1L).nome("Igreja Sede").telefone("(21) 9999-9999")
                .matriz("S").dataCad(LocalDate.now()).build();
    }

    @Test @DisplayName("Deve listar todas as igrejas")
    void deveListarTodas() {
        when(igrejaRepository.findAll()).thenReturn(List.of(igreja));
        assertEquals(1, igrejaService.listarTodas().size());
    }

    @Test @DisplayName("Deve buscar igreja por ID")
    void deveBuscarPorId() {
        when(igrejaRepository.findById(1L)).thenReturn(Optional.of(igreja));
        assertEquals("Igreja Sede", igrejaService.buscarPorId(1L).getNome());
    }

    @Test @DisplayName("Deve lançar exceção ao buscar igreja por ID inexistente")
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(igrejaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> igrejaService.buscarPorId(99L));
    }

    @Test @DisplayName("Deve salvar igreja com dataCad automática")
    void deveSalvar() {
        Igreja nova = Igreja.builder().nome("Filial").telefone("(21) 8888-8888").matriz("N").build();
        when(igrejaRepository.save(any(Igreja.class))).thenAnswer(i -> {
            Igreja saved = i.getArgument(0); saved.setId(2L); return saved;
        });
        Igreja result = igrejaService.salvar(nova);
        assertEquals(2L, result.getId());
        assertEquals(LocalDate.now(), result.getDataCad());
    }

    @Test @DisplayName("Deve atualizar igreja")
    void deveAtualizar() {
        Igreja atualizada = Igreja.builder().nome("Igreja Sede - Atualizada").telefone("(21) 9999-9999").matriz("S").build();
        when(igrejaRepository.findById(1L)).thenReturn(Optional.of(igreja));
        when(igrejaRepository.save(any(Igreja.class))).thenAnswer(i -> i.getArgument(0));
        Igreja result = igrejaService.atualizar(1L, atualizada);
        assertEquals("Igreja Sede - Atualizada", result.getNome());
        assertEquals(1L, result.getId());
    }

    @Test @DisplayName("Deve deletar igreja")
    void deveDeletar() {
        when(igrejaRepository.existsById(1L)).thenReturn(true);
        igrejaService.deletar(1L);
        verify(igrejaRepository).deleteById(1L);
    }

    @Test @DisplayName("Deve contar total de igrejas")
    void deveContarTotal() {
        when(igrejaRepository.count()).thenReturn(3L);
        assertEquals(3L, igrejaService.contarTotal());
    }
}
