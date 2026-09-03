package com.sg.tesoureiros;

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
@DisplayName("TesoureiroService - Testes de CRUD")
class TesoureiroServiceTest {

    @Mock private TesoureiroRepository tesoureiroRepository;
    @InjectMocks private TesoureiroService tesoureiroService;

    private Tesoureiro tesoureiro;

    @BeforeEach
    void setUp() {
        tesoureiro = Tesoureiro.builder().id(1L).nome("Tes. João").email("joao@email.com")
                .dataCad(LocalDate.now()).build();
    }

    @Test void deveListarTodos() {
        when(tesoureiroRepository.findAll()).thenReturn(List.of(tesoureiro));
        assertEquals(1, tesoureiroService.listarTodos().size());
    }

    @Test void deveBuscarPorId() {
        when(tesoureiroRepository.findById(1L)).thenReturn(Optional.of(tesoureiro));
        assertEquals("Tes. João", tesoureiroService.buscarPorId(1L).getNome());
    }

    @Test void deveLancarExcecaoAoBuscarIdInexistente() {
        when(tesoureiroRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> tesoureiroService.buscarPorId(99L));
    }

    @Test void deveSalvarComDataCadAutomatica() {
        Tesoureiro novo = Tesoureiro.builder().nome("Tes. Maria").email("maria@email.com").build();
        when(tesoureiroRepository.save(any(Tesoureiro.class))).thenAnswer(i -> {
            Tesoureiro saved = i.getArgument(0); saved.setId(2L); return saved;
        });
        Tesoureiro result = tesoureiroService.salvar(novo);
        assertEquals(2L, result.getId());
        assertEquals(LocalDate.now(), result.getDataCad());
    }

    @Test void deveAtualizar() {
        Tesoureiro atualizado = Tesoureiro.builder().nome("Tes. João Pedro").email("joao@email.com").build();
        when(tesoureiroRepository.findById(1L)).thenReturn(Optional.of(tesoureiro));
        when(tesoureiroRepository.save(any(Tesoureiro.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals("Tes. João Pedro", tesoureiroService.atualizar(1L, atualizado).getNome());
    }

    @Test void deveDeletar() {
        when(tesoureiroRepository.existsById(1L)).thenReturn(true);
        tesoureiroService.deletar(1L);
        verify(tesoureiroRepository).deleteById(1L);
    }
}
