package com.sg.tarefas;

import com.sg.shared.enums.StatusTarefa;
import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;

    public TarefaService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public List<Tarefa> listarTodas() {
        return tarefaRepository.findAll();
    }

    public Tarefa buscarPorId(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));
    }

    public Tarefa salvar(Tarefa tarefa) {
        if (tarefa.getStatusTarefa() == null) tarefa.setStatusTarefa(StatusTarefa.PENDENTE);
        return tarefaRepository.save(tarefa);
    }

    public Tarefa atualizar(Long id, Tarefa tarefa) {
        buscarPorId(id);
        tarefa.setId(id);
        return tarefaRepository.save(tarefa);
    }

    public Tarefa concluir(Long id) {
        Tarefa tarefa = buscarPorId(id);
        tarefa.setStatusTarefa(StatusTarefa.CONCLUIDA);
        return tarefaRepository.save(tarefa);
    }

    public void deletar(Long id) {
        if (!tarefaRepository.existsById(id))
            throw new ResourceNotFoundException("Tarefa", id);
        tarefaRepository.deleteById(id);
    }
}
