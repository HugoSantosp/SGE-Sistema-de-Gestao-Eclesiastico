package com.sg.cargos;

import com.sg.shared.exceptions.BusinessException;
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
@DisplayName("CargoService - Testes de Cargos Eclesiásticos")
class CargoServiceTest {

    @Mock
    private CargoRepository cargoRepository;

    @InjectMocks
    private CargoService cargoService;

    private Cargo cargo;

    @BeforeEach
    void setUp() {
        cargo = Cargo.builder().id(1L).nome("Presbítero").build();
    }

    @Test
    @DisplayName("Deve listar todos os cargos")
    void deveListarTodos() {
        when(cargoRepository.findAll()).thenReturn(List.of(cargo));

        var result = cargoService.listarTodos();

        assertEquals(1, result.size());
        assertEquals("Presbítero", result.get(0).getNome());
    }

    @Test
    @DisplayName("Deve buscar cargo por ID com sucesso")
    void deveBuscarPorId() {
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));

        Cargo result = cargoService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals("Presbítero", result.getNome());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar cargo por ID inexistente")
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(cargoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cargoService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Deve criar cargo com sucesso")
    void deveCriarCargo() {
        Cargo novoCargo = Cargo.builder().nome("Diácono").build();

        when(cargoRepository.existsByNome("Diácono")).thenReturn(false);
        when(cargoRepository.save(any(Cargo.class))).thenAnswer(i -> {
            Cargo saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        Cargo result = cargoService.salvar(novoCargo);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Diácono", result.getNome());
        verify(cargoRepository).existsByNome("Diácono");
        verify(cargoRepository).save(any(Cargo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cargo com nome duplicado")
    void deveLancarExcecaoAoCriarCargoComNomeDuplicado() {
        Cargo novoCargo = Cargo.builder().nome("Presbítero").build();

        when(cargoRepository.existsByNome("Presbítero")).thenReturn(true);

        assertThrows(BusinessException.class, () -> cargoService.salvar(novoCargo));
        verify(cargoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar cargo com sucesso")
    void deveAtualizarCargo() {
        Cargo cargoAtualizado = Cargo.builder().nome("Presbítero Sênior").build();

        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));
        when(cargoRepository.save(any(Cargo.class))).thenAnswer(i -> i.getArgument(0));

        Cargo result = cargoService.atualizar(1L, cargoAtualizado);

        assertEquals("Presbítero Sênior", result.getNome());
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Deve deletar cargo com sucesso")
    void deveDeletarCargo() {
        when(cargoRepository.existsById(1L)).thenReturn(true);

        cargoService.deletar(1L);

        verify(cargoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar cargo inexistente")
    void deveLancarExcecaoAoDeletarCargoInexistente() {
        when(cargoRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> cargoService.deletar(99L));
        verify(cargoRepository, never()).deleteById(anyLong());
    }
}
