package com.sg.financeiro;

import com.sg.shared.enums.FrequenciaPagamento;
import com.sg.shared.enums.StatusConta;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinanceiroService - Testes de Contas a Pagar e Receber")
class FinanceiroServiceTest {

    @Mock private ContaPagarRepository contaPagarRepository;
    @Mock private ContaReceberRepository contaReceberRepository;
    @Mock private PagamentoDespesaRepository pagamentoDespesaRepository;

    @InjectMocks
    private FinanceiroService financeiroService;

    private ContaPagar contaPagar;
    private ContaReceber contaReceber;

    @BeforeEach
    void setUp() {
        contaPagar = ContaPagar.builder()
                .id(1L).descricao("Conta de Luz").valor(new BigDecimal("150.00"))
                .vencimento(LocalDate.of(2026, 8, 15))
                .dataCad(LocalDate.of(2026, 7, 1))
                .frequencia(FrequenciaPagamento.MENSAL)
                .status(StatusConta.PENDENTE).build();

        contaReceber = ContaReceber.builder()
                .id(1L).descricao("Dízimo").valor(new BigDecimal("500.00"))
                .vencimento(LocalDate.of(2026, 8, 10))
                .dataCad(LocalDate.of(2026, 7, 1))
                .frequencia(FrequenciaPagamento.MENSAL)
                .status(StatusConta.PENDENTE).build();
    }

    // ===== CONTAS A PAGAR =====

    @Test
    @DisplayName("Deve listar todas as contas a pagar")
    void deveListarTodasContasPagar() {
        when(contaPagarRepository.findAll()).thenReturn(List.of(contaPagar));

        List<ContaPagar> result = financeiroService.listarContasPagar();

        assertEquals(1, result.size());
        verify(contaPagarRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar conta a pagar por ID com sucesso")
    void deveBuscarContaPagarPorId() {
        when(contaPagarRepository.findById(1L)).thenReturn(Optional.of(contaPagar));

        ContaPagar result = financeiroService.buscarContaPagar(1L);

        assertNotNull(result);
        assertEquals("Conta de Luz", result.getDescricao());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar conta a pagar por ID inexistente")
    void deveLancarExcecaoAoBuscarContaPagarIdInexistente() {
        when(contaPagarRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> financeiroService.buscarContaPagar(99L));
    }

    @Test
    @DisplayName("Deve salvar conta a pagar com dataCad e status padrão")
    void deveSalvarContaPagar() {
        ContaPagar novaConta = ContaPagar.builder()
                .descricao("Aluguel").valor(new BigDecimal("2000.00"))
                .vencimento(LocalDate.of(2026, 8, 1))
                .frequencia(FrequenciaPagamento.MENSAL).build();

        when(contaPagarRepository.save(any(ContaPagar.class))).thenAnswer(i -> {
            ContaPagar saved = i.getArgument(0);
            if (saved.getId() == null) saved.setId(2L);
            return saved;
        });

        ContaPagar result = financeiroService.salvarContaPagar(novaConta);

        assertNotNull(result.getDataCad());
        assertEquals(LocalDate.now(), result.getDataCad());
        assertEquals(StatusConta.PENDENTE, result.getStatus());
        verify(contaPagarRepository).save(any(ContaPagar.class));
    }

    @Test
    @DisplayName("Deve baixar conta a pagar (mudar status para PAGA)")
    void deveBaixarContaPagar() {
        when(contaPagarRepository.findById(1L)).thenReturn(Optional.of(contaPagar));
        when(contaPagarRepository.save(any(ContaPagar.class))).thenAnswer(i -> i.getArgument(0));

        ContaPagar result = financeiroService.baixarContaPagar(1L);

        assertEquals(StatusConta.PAGA, result.getStatus());
        assertNotNull(result.getDataBaixa());
        assertEquals(LocalDate.now(), result.getDataBaixa());
    }

    @Test
    @DisplayName("Deve lançar exceção ao baixar conta a pagar inexistente")
    void deveLancarExcecaoAoBaixarContaInexistente() {
        when(contaPagarRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> financeiroService.baixarContaPagar(99L));
    }

    @Test
    @DisplayName("Deve deletar conta a pagar com sucesso")
    void deveDeletarContaPagar() {
        when(contaPagarRepository.findById(1L)).thenReturn(Optional.of(contaPagar));

        financeiroService.deletarContaPagar(1L);

        verify(contaPagarRepository).delete(contaPagar);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar conta a pagar inexistente")
    void deveLancarExcecaoAoDeletarContaPagarInexistente() {
        when(contaPagarRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> financeiroService.deletarContaPagar(99L));
        verify(contaPagarRepository, never()).delete(any(ContaPagar.class));
    }

    // ===== CONTAS A RECEBER =====

    @Test
    @DisplayName("Deve listar todas as contas a receber")
    void deveListarTodasContasReceber() {
        when(contaReceberRepository.findAll()).thenReturn(List.of(contaReceber));

        List<ContaReceber> result = financeiroService.listarContasReceber();

        assertEquals(1, result.size());
        verify(contaReceberRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar conta a receber por ID com sucesso")
    void deveBuscarContaReceberPorId() {
        when(contaReceberRepository.findById(1L)).thenReturn(Optional.of(contaReceber));

        ContaReceber result = financeiroService.buscarContaReceber(1L);

        assertNotNull(result);
        assertEquals("Dízimo", result.getDescricao());
    }

    @Test
    @DisplayName("Deve salvar conta a receber com dataCad e status padrão")
    void deveSalvarContaReceber() {
        ContaReceber novaConta = ContaReceber.builder()
                .descricao("Oferta").valor(new BigDecimal("300.00"))
                .vencimento(LocalDate.of(2026, 8, 1))
                .frequencia(FrequenciaPagamento.MENSAL).build();

        when(contaReceberRepository.save(any(ContaReceber.class))).thenAnswer(i -> {
            ContaReceber saved = i.getArgument(0);
            if (saved.getId() == null) saved.setId(2L);
            return saved;
        });

        ContaReceber result = financeiroService.salvarContaReceber(novaConta);

        assertNotNull(result.getDataCad());
        assertEquals(LocalDate.now(), result.getDataCad());
        assertEquals(StatusConta.PENDENTE, result.getStatus());
    }

    @Test
    @DisplayName("Deve receber conta (mudar status para PAGA)")
    void deveReceberConta() {
        when(contaReceberRepository.findById(1L)).thenReturn(Optional.of(contaReceber));
        when(contaReceberRepository.save(any(ContaReceber.class))).thenAnswer(i -> i.getArgument(0));

        ContaReceber result = financeiroService.receberConta(1L);

        assertEquals(StatusConta.PAGA, result.getStatus());
        assertNotNull(result.getDataRecebimento());
        assertEquals(LocalDate.now(), result.getDataRecebimento());
    }

    @Test
    @DisplayName("Deve lançar exceção ao receber conta inexistente")
    void deveLancarExcecaoAoReceberContaInexistente() {
        when(contaReceberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> financeiroService.receberConta(99L));
    }

    @Test
    @DisplayName("Deve deletar conta a receber com sucesso")
    void deveDeletarContaReceber() {
        when(contaReceberRepository.findById(1L)).thenReturn(Optional.of(contaReceber));

        financeiroService.deletarContaReceber(1L);

        verify(contaReceberRepository).delete(contaReceber);
    }

    // ===== ALTERAÇÃO MANUAL DE STATUS =====

    @Test
    @DisplayName("Deve marcar conta a pagar como paga e preencher a data de baixa")
    void deveAlterarStatusContaPagarParaPaga() {
        when(contaPagarRepository.findById(1L)).thenReturn(Optional.of(contaPagar));
        when(pagamentoDespesaRepository.totalPagoPorConta(1L)).thenReturn(BigDecimal.ZERO);
        when(contaPagarRepository.save(any(ContaPagar.class))).thenAnswer(i -> i.getArgument(0));

        ContaPagar result = financeiroService.alterarStatusContaPagar(1L, StatusConta.PAGA);

        assertEquals(StatusConta.PAGA, result.getStatus());
        assertEquals(LocalDate.now(), result.getDataBaixa());
    }

    @Test
    @DisplayName("Deve voltar conta a pagar para pendente e limpar a data de baixa")
    void deveAlterarStatusContaPagarParaPendente() {
        contaPagar.setStatus(StatusConta.PAGA);
        contaPagar.setDataBaixa(LocalDate.now());
        when(contaPagarRepository.findById(1L)).thenReturn(Optional.of(contaPagar));
        when(pagamentoDespesaRepository.totalPagoPorConta(1L)).thenReturn(BigDecimal.ZERO);
        when(contaPagarRepository.save(any(ContaPagar.class))).thenAnswer(i -> i.getArgument(0));

        ContaPagar result = financeiroService.alterarStatusContaPagar(1L, StatusConta.PENDENTE);

        assertEquals(StatusConta.PENDENTE, result.getStatus());
        assertNull(result.getDataBaixa());
    }

    @Test
    @DisplayName("Não deve marcar como paga conta com pagamento parcial")
    void naoDeveMarcarComoPagaContaComPagamentoParcial() {
        when(contaPagarRepository.findById(1L)).thenReturn(Optional.of(contaPagar));
        when(pagamentoDespesaRepository.totalPagoPorConta(1L)).thenReturn(new BigDecimal("50.00"));

        assertThrows(com.sg.shared.exceptions.BusinessException.class,
                () -> financeiroService.alterarStatusContaPagar(1L, StatusConta.PAGA));
        verify(contaPagarRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve cancelar pelo endpoint de status")
    void naoDeveCancelarPeloEndpointDeStatus() {
        when(contaPagarRepository.findById(1L)).thenReturn(Optional.of(contaPagar));

        assertThrows(com.sg.shared.exceptions.BusinessException.class,
                () -> financeiroService.alterarStatusContaPagar(1L, StatusConta.CANCELADA));
    }

    @Test
    @DisplayName("Deve marcar conta a receber como paga e preencher a data de recebimento")
    void deveAlterarStatusContaReceberParaPaga() {
        when(contaReceberRepository.findById(1L)).thenReturn(Optional.of(contaReceber));
        when(contaReceberRepository.save(any(ContaReceber.class))).thenAnswer(i -> i.getArgument(0));

        ContaReceber result = financeiroService.alterarStatusContaReceber(1L, StatusConta.PAGA);

        assertEquals(StatusConta.PAGA, result.getStatus());
        assertEquals(LocalDate.now(), result.getDataRecebimento());
    }

    @Test
    @DisplayName("Deve voltar conta a receber para vencida e limpar a data de recebimento")
    void deveAlterarStatusContaReceberParaVencida() {
        contaReceber.setStatus(StatusConta.PAGA);
        contaReceber.setDataRecebimento(LocalDate.now());
        when(contaReceberRepository.findById(1L)).thenReturn(Optional.of(contaReceber));
        when(contaReceberRepository.save(any(ContaReceber.class))).thenAnswer(i -> i.getArgument(0));

        ContaReceber result = financeiroService.alterarStatusContaReceber(1L, StatusConta.VENCIDA);

        assertEquals(StatusConta.VENCIDA, result.getStatus());
        assertNull(result.getDataRecebimento());
    }
}
