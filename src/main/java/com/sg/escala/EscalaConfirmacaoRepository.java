package com.sg.escala;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaConfirmacaoRepository extends JpaRepository<EscalaConfirmacao, Long> {

    List<EscalaConfirmacao> findByEscalaId(Long escalaId);

    java.util.Optional<EscalaConfirmacao> findByEscalaIdAndMembroId(Long escalaId, Long membroId);
}
