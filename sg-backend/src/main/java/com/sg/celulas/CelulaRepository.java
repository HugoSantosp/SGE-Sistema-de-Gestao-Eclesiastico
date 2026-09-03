package com.sg.celulas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CelulaRepository extends JpaRepository<Celula, Long> {
}
