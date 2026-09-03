package com.sg.financeiro;

import com.sg.shared.enums.StatusConta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContaPagarRepository extends JpaRepository<ContaPagar, Long> {
    List<ContaPagar> findByStatus(StatusConta status);
    List<ContaPagar> findByVencimentoBeforeAndStatus(LocalDate date, StatusConta status);
}
