package com.sg.financeiro;

import com.sg.financeiro.dto.DoacaoRequestDTO;
import com.sg.membros.Membro;
import com.sg.membros.MembroRepository;
import com.sg.shared.enums.FormaPagamento;
import com.sg.shared.enums.TipoDoacao;
import com.sg.shared.exceptions.BusinessException;
import com.sg.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoacaoService - Testes de Doações")
class DoacaoServiceTest {

    @Mock private DoacaoRepository doacaoRepository;
    @Mock private MembroRepository membroRepository;
    @InjectMocks private DoacaoService doacaoService;

    private DoacaoRequestDTO dizimo;

    @BeforeEach
    void setUp() {
        dizimo = new DoacaoRequestDTO(
                TipoDoacao.DIZIMO, 7L, null,
                LocalDate.of(2026, 11, 15), FormaPagamento.PIX,
                new BigDecimal("150.00"), "Dízimo de novembro", null);
    }

    @Test
    @DisplayName("Dízimo com membro válido copia o nome do membro para o doador")
    void dizimoComMembroValido() {
        Membro membro = Membro.builder().id(7L).nome("Irmã Maria Souza").build();
        when(membroRepository.findById(7L)).thenReturn(Optional.of(membro));
        when(doacaoRepository.save(any(Doacao.class))).thenAnswer(i -> i.getArgument(0));

        Doacao salva = doacaoService.salvar(dizimo);

        assertEquals(TipoDoacao.DIZIMO, salva.getTipo());
        assertEquals(7L, salva.getMembroId());
        assertEquals("Irmã Maria Souza", salva.getNomeDoador());
        assertNotNull(salva.getDataCad());
    }

    @Test
    @DisplayName("Oferta sem membro grava o nome livre do doador")
    void ofertaSemMembro() {
        DoacaoRequestDTO oferta = new DoacaoRequestDTO(
                TipoDoacao.OFERTA, null, "  Visitante João  ",
                LocalDate.of(2026, 11, 16), FormaPagamento.DINHEIRO,
                new BigDecimal("50.00"), null, null);
        when(doacaoRepository.save(any(Doacao.class))).thenAnswer(i -> i.getArgument(0));

        Doacao salva = doacaoService.salvar(oferta);

        assertNull(salva.getMembroId());
        assertEquals("Visitante João", salva.getNomeDoador());
        verifyNoInteractions(membroRepository);
    }

    @Test
    @DisplayName("Oferta sem nome do doador é recusada com 422")
    void ofertaSemNomeRecusada() {
        DoacaoRequestDTO oferta = new DoacaoRequestDTO(
                TipoDoacao.OFERTA, null, "   ",
                LocalDate.of(2026, 11, 16), FormaPagamento.DINHEIRO,
                new BigDecimal("50.00"), null, null);

        assertThrows(BusinessException.class, () -> doacaoService.salvar(oferta));
        verify(doacaoRepository, never()).save(any(Doacao.class));
    }

    @Test
    @DisplayName("Dízimo sem membro é recusado com 422")
    void dizimoSemMembroRecusado() {
        DoacaoRequestDTO semMembro = new DoacaoRequestDTO(
                TipoDoacao.DIZIMO, null, null,
                LocalDate.of(2026, 11, 15), FormaPagamento.PIX,
                new BigDecimal("150.00"), null, null);

        assertThrows(BusinessException.class, () -> doacaoService.salvar(semMembro));
        verify(doacaoRepository, never()).save(any(Doacao.class));
    }

    @Test
    @DisplayName("Membro inexistente gera 404 e não salva a doação")
    void membroInexistenteNaoSalva() {
        when(membroRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> doacaoService.salvar(dizimo));
        verify(doacaoRepository, never()).save(any(Doacao.class));
    }

    @Test
    @DisplayName("Filtro por tipo consulta apenas o tipo informado")
    void filtroPorTipo() {
        Doacao doacao = Doacao.builder().id(1L).tipo(TipoDoacao.PRIMICIA)
                .nomeDoador("Irmão Pedro").valor(new BigDecimal("80.00"))
                .dataDoacao(LocalDate.of(2026, 11, 10))
                .formaPagamento(FormaPagamento.PIX).build();
        when(doacaoRepository.filtrar(TipoDoacao.PRIMICIA, null, null, null, null))
                .thenReturn(List.of(doacao));

        List<Doacao> resultado = doacaoService.listar(TipoDoacao.PRIMICIA, null, null);

        assertEquals(1, resultado.size());
        assertEquals(TipoDoacao.PRIMICIA, resultado.get(0).getTipo());
        verify(doacaoRepository, never()).findAllByOrderByDataDoacaoDescIdDesc();
    }
}
