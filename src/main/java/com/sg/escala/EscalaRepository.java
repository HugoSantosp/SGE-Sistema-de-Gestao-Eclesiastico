package com.sg.escala;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Long> {

    Optional<Escala> findByPublicTokenAndAbertaTrue(String publicToken);

    Optional<Escala> findByResultadoToken(String resultadoToken);

    Optional<Escala> findByPublicToken(String publicToken);

    List<Escala> findByMinisterioId(Long ministerioId);

    List<Escala> findByMinisterioIdIn(java.util.Collection<Long> ministerioIds);
}
