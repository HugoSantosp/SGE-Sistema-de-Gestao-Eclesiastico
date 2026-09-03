package com.sg.ministerios;

import com.sg.shared.enums.PapelMinisterio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MinisterioMembroRepository extends JpaRepository<MinisterioMembro, Long> {

    List<MinisterioMembro> findByMembroId(Long membroId);

    List<MinisterioMembro> findByMinisterioId(Long ministerioId);

    List<MinisterioMembro> findByMinisterioIdAndPapel(Long ministerioId, PapelMinisterio papel);

    boolean existsByMinisterioIdAndMembroId(Long ministerioId, Long membroId);

    boolean existsByMinisterioIdAndMembroIdAndPapel(Long ministerioId, Long membroId, PapelMinisterio papel);

    java.util.Optional<MinisterioMembro> findByMinisterioIdAndMembroId(Long ministerioId, Long membroId);

    void deleteByMembroId(Long membroId);
}
