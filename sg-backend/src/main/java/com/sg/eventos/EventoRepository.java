package com.sg.eventos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    @Query("SELECT e FROM Evento e WHERE e.data >= :hoje ORDER BY e.data ASC")
    List<Evento> findProximos(LocalDate hoje);

    @Query("SELECT e FROM Evento e WHERE YEAR(e.data) = :ano AND MONTH(e.data) = :mes ORDER BY e.data ASC")
    List<Evento> findByMesAno(int mes, int ano);
}
