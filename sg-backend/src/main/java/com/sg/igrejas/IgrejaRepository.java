package com.sg.igrejas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IgrejaRepository extends JpaRepository<Igreja, Long> {
    List<Igreja> findByMatriz(String matriz);
}
