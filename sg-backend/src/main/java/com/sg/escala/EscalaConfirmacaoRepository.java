package com.sg.escala;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaConfirmacaoRepository extends JpaRepository<EscalaConfirmacao, Long> {

    List<EscalaConfirmacao> findByEscalaId(Long escalaId);

    java.util.Optional<EscalaConfirmacao> findByEscalaIdAndMembroId(Long escalaId, Long membroId);

    /**
     * Confirmações do membro em outras escalas que possuem ministério (escalas genéricas
     * ficam de fora da regra de conflito). Traz as datas e a escala já carregadas para
     * evitar N+1 na checagem de choque de data + horário.
     */
    @Query("""
            select distinct c from EscalaConfirmacao c
            join fetch c.datas d
            join fetch c.escala e
            where c.membro.id = :membroId
              and c.escala.id <> :escalaId
              and e.ministerioId is not null
            """)
    List<EscalaConfirmacao> buscarConfirmacoesEmOutrasEscalas(
            @Param("membroId") Long membroId,
            @Param("escalaId") Long escalaId);
}
