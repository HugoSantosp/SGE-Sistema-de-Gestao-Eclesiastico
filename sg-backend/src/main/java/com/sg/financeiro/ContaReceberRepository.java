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
public interface ContaReceberRepository extends JpaRepository<ContaReceber, Long> {
    List<ContaReceber> findByStatus(StatusConta status);
    List<ContaReceber> findByVencimentoBeforeAndStatus(LocalDate date, StatusConta status);
    boolean existsByCategoriaId(Long categoriaId);

    List<ContaReceber> findByStatusAndDataRecebimentoBetween(StatusConta status, LocalDate inicio, LocalDate fim);

    @Query("""
            select coalesce(sum(c.valor), 0) from ContaReceber c
            where c.status = com.sg.shared.enums.StatusConta.PAGA
              and c.dataRecebimento between :inicio and :fim
            """)
    BigDecimal somarRecebidasNoPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
            select coalesce(sum(c.valor), 0) from ContaReceber c
            where c.status = com.sg.shared.enums.StatusConta.PAGA
              and c.dataRecebimento <= :data
            """)
    BigDecimal somarRecebidasAte(@Param("data") LocalDate data);
}
