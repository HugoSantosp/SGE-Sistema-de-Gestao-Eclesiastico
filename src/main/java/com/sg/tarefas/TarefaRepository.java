package com.sg.tarefas;

import com.sg.shared.enums.StatusTarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    List<Tarefa> findByStatusTarefa(StatusTarefa status);
    List<Tarefa> findByDataTarefa(LocalDate data);
}
