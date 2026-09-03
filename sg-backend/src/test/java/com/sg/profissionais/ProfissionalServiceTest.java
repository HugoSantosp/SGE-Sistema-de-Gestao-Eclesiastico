package com.sg.profissionais;

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
@DisplayName("ProfissionalService - Testes de Profissionais (Mural)")
class ProfissionalServiceTest {

    @Mock private ProfissionalRepository profissionalRepository;
    @InjectMocks private ProfissionalService profissionalService;

    private Profissional profissional;

    @BeforeEach
    void setUp() {
        profissional = Profissional.builder().id(1L).nome("Dr. Carlos")
                .especialidade("Clínico Geral").telefone("(21) 9999-9999")
                .descricao("Médico experiente").build();
    }

    @Test @DisplayName("Deve listar todos os profissionais")
    void deveListarTodos() {
        when(profissionalRepository.findAll()).thenReturn(List.of(profissional));
        assertEquals(1, profissionalService.listarTodos().size());
    }

    @Test @DisplayName("Deve listar profissionais públicos")
    void deveListarPublicos() {
        when(profissionalRepository.findAll()).thenReturn(List.of(profissional));
        assertEquals(1, profissionalService.listarPublicos().size());
        verify(profissionalRepository).findAll();
    }

    @Test @DisplayName("Deve buscar profissional por ID")
    void deveBuscarPorId() {
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        assertEquals("Dr. Carlos", profissionalService.buscarPorId(1L).getNome());
    }

    @Test @DisplayName("Deve salvar profissional")
    void deveSalvar() {
        when(profissionalRepository.save(any(Profissional.class))).thenAnswer(i -> i.getArgument(0));
        assertNotNull(profissionalService.salvar(profissional));
    }

    @Test @DisplayName("Deve deletar profissional")
    void deveDeletar() {
        when(profissionalRepository.existsById(1L)).thenReturn(true);
        profissionalService.deletar(1L);
        verify(profissionalRepository).deleteById(1L);
    }
}
