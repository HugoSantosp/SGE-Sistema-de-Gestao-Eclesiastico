package com.sg.celulas;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CelulaService {
    private final CelulaRepository celulaRepository;

    public CelulaService(CelulaRepository celulaRepository) {
        this.celulaRepository = celulaRepository;
    }

    @Transactional(readOnly = true)
    public List<Celula> listarTodos() {
        return celulaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Celula> listarPublicas() {
        return celulaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Celula buscarPorId(Long id) {
        return celulaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Célula", id));
    }

    @Transactional
    public Celula salvar(Celula celula) { return celulaRepository.save(celula); }

    @Transactional
    public void deletar(Long id) {
        if (!celulaRepository.existsById(id)) throw new ResourceNotFoundException("Célula", id);
        celulaRepository.deleteById(id);
    }
}
