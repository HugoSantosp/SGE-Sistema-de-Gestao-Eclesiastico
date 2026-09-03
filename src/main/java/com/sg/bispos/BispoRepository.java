package com.sg.bispos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BispoRepository extends JpaRepository<Bispo, Long> {
    boolean existsByEmail(String email);
    java.util.Optional<Bispo> findByEmail(String email);
}
