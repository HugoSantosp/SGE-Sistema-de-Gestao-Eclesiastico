package com.sg.secretarios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecretarioRepository extends JpaRepository<Secretario, Long> {
    java.util.Optional<Secretario> findByEmail(String email);
}
