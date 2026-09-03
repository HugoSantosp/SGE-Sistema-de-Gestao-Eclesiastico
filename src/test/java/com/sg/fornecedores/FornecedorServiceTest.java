package com.sg.fornecedores;

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
@DisplayName("FornecedorService - Testes de Fornecedores")
class FornecedorServiceTest {

    @Mock private FornecedorRepository fornecedorRepository;
    @InjectMocks private FornecedorService fornecedorService;

    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        fornecedor = Fornecedor.builder().id(1L).nome("Papelaria ABC")
                .telefone("(21) 9999-9999").produto("Papéis").build();
    }

    @Test @DisplayName("Deve listar todos os fornecedores")
    void deveListarTodos() {
        when(fornecedorRepository.findAll()).thenReturn(List.of(fornecedor));
        assertEquals(1, fornecedorService.listarTodos().size());
    }

    @Test @DisplayName("Deve buscar fornecedor por ID")
    void deveBuscarPorId() {
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        assertEquals("Papelaria ABC", fornecedorService.buscarPorId(1L).getNome());
    }

    @Test @DisplayName("Deve salvar fornecedor")
    void deveSalvar() {
        when(fornecedorRepository.save(any(Fornecedor.class))).thenAnswer(i -> i.getArgument(0));
        assertNotNull(fornecedorService.salvar(fornecedor));
    }

    @Test @DisplayName("Deve atualizar fornecedor")
    void deveAtualizar() {
        Fornecedor atualizado = Fornecedor.builder().nome("Papelaria ABC Ltda").telefone("(21) 9999-9999").produto("Papéis e Materiais").build();
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.save(any(Fornecedor.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals("Papelaria ABC Ltda", fornecedorService.atualizar(1L, atualizado).getNome());
    }

    @Test @DisplayName("Deve deletar fornecedor")
    void deveDeletar() {
        when(fornecedorRepository.existsById(1L)).thenReturn(true);
        fornecedorService.deletar(1L);
        verify(fornecedorRepository).deleteById(1L);
    }

    @Test @DisplayName("Deve lançar exceção ao deletar fornecedor inexistente")
    void deveLancarExcecaoAoDeletarInexistente() {
        when(fornecedorRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> fornecedorService.deletar(99L));
    }
}
