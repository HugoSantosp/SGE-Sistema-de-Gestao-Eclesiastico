package com.sg.membros;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MembroRepository extends JpaRepository<Membro, Long> {
    List<Membro> findBySituacao(com.sg.shared.enums.StatusMembro situacao);
    long countBySituacao(com.sg.shared.enums.StatusMembro situacao);
    List<Membro> findByNomeContainingIgnoreCase(String nome);
    java.util.Optional<Membro> findByDocumento(String documento);
}
