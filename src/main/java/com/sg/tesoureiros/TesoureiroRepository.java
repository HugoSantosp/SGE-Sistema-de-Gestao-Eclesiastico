package com.sg.tesoureiros;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TesoureiroRepository extends JpaRepository<Tesoureiro, Long> {
    java.util.Optional<Tesoureiro> findByEmail(String email);
}
