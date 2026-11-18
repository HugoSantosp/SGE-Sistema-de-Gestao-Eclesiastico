package com.sg.financeiro;

import com.sg.shared.enums.TipoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaFinanceiraRepository extends JpaRepository<CategoriaFinanceira, Long> {

    List<CategoriaFinanceira> findByTipoOrderByNomeAsc(TipoCategoria tipo);

    List<CategoriaFinanceira> findByTipoAndAtivoTrueOrderByNomeAsc(TipoCategoria tipo);

    Optional<CategoriaFinanceira> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndTipo(String nome, TipoCategoria tipo);
}
