package com.sg.financeiro;

import com.sg.shared.enums.FormaPagamento;
import com.sg.shared.enums.StatusDoacao;
import com.sg.shared.enums.TipoDoacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    List<Doacao> findAllByOrderByDataDoacaoDescIdDesc();

    List<Doacao> findByTipoOrderByDataDoacaoDescIdDesc(TipoDoacao tipo);

    List<Doacao> findByDataDoacaoBetweenOrderByDataDoacaoDescIdDesc(LocalDate inicio, LocalDate fim);

    boolean existsByCategoriaId(Long categoriaId);

    List<Doacao> findByStatusAndDataDoacaoBetweenOrderByDataDoacaoAscIdAsc(
            StatusDoacao status, LocalDate inicio, LocalDate fim);

    @Query("""
            select coalesce(sum(d.valor), 0) from Doacao d
            where d.status = com.sg.shared.enums.StatusDoacao.ATIVA
              and d.dataDoacao between :inicio and :fim
            """)
    BigDecimal somarAtivasNoPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
            select coalesce(sum(d.valor), 0) from Doacao d
            where d.status = com.sg.shared.enums.StatusDoacao.ATIVA
              and d.dataDoacao <= :data
            """)
    BigDecimal somarAtivasAte(@Param("data") LocalDate data);

    /**
     * Filtros combináveis. Parâmetro nulo é ignorado, o que evita uma combinação
     * de métodos derivados para cada cruzamento de tipo, período, forma e categoria.
     */
    @Query("""
            select d from Doacao d
            where (:tipo is null or d.tipo = :tipo)
              and (:inicio is null or d.dataDoacao >= :inicio)
              and (:fim is null or d.dataDoacao <= :fim)
              and (:formaPagamento is null or d.formaPagamento = :formaPagamento)
              and (:categoriaId is null or d.categoriaId = :categoriaId)
            order by d.dataDoacao desc, d.id desc
            """)
    List<Doacao> filtrar(@Param("tipo") TipoDoacao tipo,
                         @Param("inicio") LocalDate inicio,
                         @Param("fim") LocalDate fim,
                         @Param("formaPagamento") FormaPagamento formaPagamento,
                         @Param("categoriaId") Long categoriaId);
}
