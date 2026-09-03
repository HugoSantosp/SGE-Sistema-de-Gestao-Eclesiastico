package com.sg.financeiro;

import com.sg.shared.enums.StatusConta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContaReceberRepository extends JpaRepository<ContaReceber, Long> {
    List<ContaReceber> findByStatus(StatusConta status);
    List<ContaReceber> findByVencimentoBeforeAndStatus(LocalDate date, StatusConta status);
}
