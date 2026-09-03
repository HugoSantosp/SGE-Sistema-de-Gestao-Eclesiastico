package com.sg.ministerios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MinisterioRepository extends JpaRepository<Ministerio, Long> {
}
