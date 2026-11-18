package com.sg.financeiro;

import com.sg.shared.enums.StatusConta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContaPagarRepository extends JpaRepository<ContaPagar, Long> {
    List<ContaPagar> findByStatus(StatusConta status);
    List<ContaPagar> findByVencimentoBeforeAndStatus(LocalDate date, StatusConta status);
    boolean existsByCategoriaId(Long categoriaId);

    List<ContaPagar> findByStatusAndDataBaixaBetween(StatusConta status, LocalDate inicio, LocalDate fim);

    @Query("""
            select c from ContaPagar c
            where c.status = com.sg.shared.enums.StatusConta.PAGA
              and c.dataBaixa between :inicio and :fim
              and not exists (select p.id from PagamentoDespesa p where p.contaPagarId = c.id)
            """)
    List<ContaPagar> listarBaixadasSemPagamentoNoPeriodo(@Param("inicio") LocalDate inicio,
                                                         @Param("fim") LocalDate fim);

    List<ContaPagar> findByStatusInAndVencimentoBetweenOrderByVencimentoAsc(
            List<StatusConta> status, LocalDate inicio, LocalDate fim);

    List<ContaPagar> findByStatusInAndVencimentoBeforeOrderByVencimentoAsc(
            List<StatusConta> status, LocalDate data);

    /**
     * Baixas feitas pelo atalho PATCH /baixar não geram registro em pagamentos_despesa.
     * Somar apenas essas evita contar duas vezes a mesma despesa.
     */
    @Query("""
            select coalesce(sum(c.valor), 0) from ContaPagar c
            where c.status = com.sg.shared.enums.StatusConta.PAGA
              and c.dataBaixa between :inicio and :fim
              and not exists (select p.id from PagamentoDespesa p where p.contaPagarId = c.id)
            """)
    BigDecimal somarBaixadasSemPagamentoNoPeriodo(@Param("inicio") LocalDate inicio,
                                                  @Param("fim") LocalDate fim);

    @Query("""
            select coalesce(sum(c.valor), 0) from ContaPagar c
            where c.status = com.sg.shared.enums.StatusConta.PAGA
              and c.dataBaixa <= :data
              and not exists (select p.id from PagamentoDespesa p where p.contaPagarId = c.id)
            """)
    BigDecimal somarBaixadasSemPagamentoAte(@Param("data") LocalDate data);

    @Query("""
            select coalesce(sum(c.valor), 0) from ContaPagar c
            where c.status in (com.sg.shared.enums.StatusConta.PENDENTE,
                               com.sg.shared.enums.StatusConta.VENCIDA)
            """)
    BigDecimal somarValorEmAberto();
}
