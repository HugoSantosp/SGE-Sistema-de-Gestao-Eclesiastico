package com.sg.escala;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaDesignacaoRepository extends JpaRepository<EscalaDesignacao, Long> {

    List<EscalaDesignacao> findByEscalaDataId(Long escalaDataId);

    List<EscalaDesignacao> findByEscalaDataEscalaId(Long escalaId);

    void deleteByEscalaDataId(Long escalaDataId);
}
