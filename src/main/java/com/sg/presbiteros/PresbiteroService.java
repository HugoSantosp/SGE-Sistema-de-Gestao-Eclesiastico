package com.sg.presbiteros;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class PresbiteroService {

    private final PresbiteroRepository presbiteroRepository;

    public PresbiteroService(PresbiteroRepository presbiteroRepository) {
        this.presbiteroRepository = presbiteroRepository;
    }

    public List<Presbitero> listarTodos() {
        return presbiteroRepository.findAll();
    }

    public Presbitero buscarPorId(Long id) {
        return presbiteroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presbítero", id));
    }

    public Presbitero salvar(Presbitero presbitero) {
        if (presbitero.getDataCad() == null) presbitero.setDataCad(LocalDate.now());
        return presbiteroRepository.save(presbitero);
    }

    public Presbitero atualizar(Long id, Presbitero presbitero) {
        buscarPorId(id);
        presbitero.setId(id);
        return presbiteroRepository.save(presbitero);
    }

    public void deletar(Long id) {
        if (!presbiteroRepository.existsById(id))
            throw new ResourceNotFoundException("Presbítero", id);
        presbiteroRepository.deleteById(id);
    }
}
