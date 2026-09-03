package com.sg.bispos;

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
@DisplayName("BispoService - Testes de CRUD")
class BispoServiceTest {

    @Mock private BispoRepository bispoRepository;
    @InjectMocks private BispoService bispoService;

    private Bispo bispo;

    @BeforeEach
    void setUp() {
        bispo = Bispo.builder().id(1L).nome("Dom Pedro").email("pedro@email.com")
                .documento("12345678900").dataCad(LocalDate.now()).build();
    }

    @Test void deveListarTodos() {
        when(bispoRepository.findAll()).thenReturn(List.of(bispo));
        assertEquals(1, bispoService.listarTodos().size());
    }

    @Test void deveBuscarPorId() {
        when(bispoRepository.findById(1L)).thenReturn(Optional.of(bispo));
        assertEquals("Dom Pedro", bispoService.buscarPorId(1L).getNome());
    }

    @Test void deveLancarExcecaoAoBuscarIdInexistente() {
        when(bispoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bispoService.buscarPorId(99L));
    }

    @Test void deveSalvarComDataCadAutomatica() {
        Bispo novo = Bispo.builder().nome("Dom Marcos").email("marcos@email.com").build();
        when(bispoRepository.save(any(Bispo.class))).thenAnswer(i -> {
            Bispo saved = i.getArgument(0); saved.setId(2L); return saved;
        });
        Bispo result = bispoService.salvar(novo);
        assertEquals(2L, result.getId());
        assertNotNull(result.getDataCad());
        assertEquals(LocalDate.now(), result.getDataCad());
    }

    @Test void deveAtualizar() {
        Bispo atualizado = Bispo.builder().nome("Dom Pedro II").email("pedro2@email.com").build();
        when(bispoRepository.findById(1L)).thenReturn(Optional.of(bispo));
        when(bispoRepository.save(any(Bispo.class))).thenAnswer(i -> i.getArgument(0));
        Bispo result = bispoService.atualizar(1L, atualizado);
        assertEquals("Dom Pedro II", result.getNome());
        assertEquals(1L, result.getId());
    }

    @Test void deveDeletar() {
        when(bispoRepository.existsById(1L)).thenReturn(true);
        bispoService.deletar(1L);
        verify(bispoRepository).deleteById(1L);
    }

    @Test void deveLancarExcecaoAoDeletarInexistente() {
        when(bispoRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> bispoService.deletar(99L));
    }
}
