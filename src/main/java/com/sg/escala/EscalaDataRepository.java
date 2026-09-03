package com.sg.escala;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaDataRepository extends JpaRepository<EscalaData, Long> {

    List<EscalaData> findByEscalaIdOrderByDataAscHorarioAsc(Long escalaId);
}
