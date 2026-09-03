package com.sg.presbiteros;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PresbiteroRepository extends JpaRepository<Presbitero, Long> {
    java.util.Optional<Presbitero> findByEmail(String email);
}
