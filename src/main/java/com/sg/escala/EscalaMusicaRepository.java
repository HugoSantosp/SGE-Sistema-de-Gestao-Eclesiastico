package com.sg.escala;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaMusicaRepository extends JpaRepository<EscalaMusica, Long> {

    List<EscalaMusica> findByEscalaDataIdOrderByOrdemAsc(Long escalaDataId);

    void deleteByEscalaDataId(Long escalaDataId);
}
