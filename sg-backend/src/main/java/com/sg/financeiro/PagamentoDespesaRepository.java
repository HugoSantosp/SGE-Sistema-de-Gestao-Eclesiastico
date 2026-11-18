package com.sg.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PagamentoDespesaRepository extends JpaRepository<PagamentoDespesa, Long> {

    List<PagamentoDespesa> findByContaPagarIdOrderByDataPagamentoAscIdAsc(Long contaPagarId);

    List<PagamentoDespesa> findByDataPagamentoBetweenOrderByDataPagamentoAscIdAsc(LocalDate inicio, LocalDate fim);

    /** Retorna null quando a conta ainda não tem pagamento; o service converte para ZERO. */
    @Query("select sum(p.valorPago) from PagamentoDespesa p where p.contaPagarId = :contaPagarId")
    BigDecimal totalPagoPorConta(@Param("contaPagarId") Long contaPagarId);

    @Query("""
            select coalesce(sum(p.valorPago), 0) from PagamentoDespesa p
            where p.dataPagamento between :inicio and :fim
            """)
    BigDecimal somarPagosNoPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
            select coalesce(sum(p.valorPago), 0) from PagamentoDespesa p
            where p.dataPagamento <= :data
            """)
    BigDecimal somarPagosAte(@Param("data") LocalDate data);

    /** Total pago de contas que ainda não estão quitadas nem canceladas. */
    @Query("""
            select coalesce(sum(p.valorPago), 0) from PagamentoDespesa p
            where p.contaPagarId in (
                select c.id from ContaPagar c
                where c.status in (com.sg.shared.enums.StatusConta.PENDENTE,
                                   com.sg.shared.enums.StatusConta.VENCIDA))
            """)
    BigDecimal somarPagosEmContasAbertas();
}
