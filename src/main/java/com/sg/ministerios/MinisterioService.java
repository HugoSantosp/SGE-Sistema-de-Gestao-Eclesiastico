package com.sg.ministerios;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MinisterioService {

    private final MinisterioRepository ministerioRepository;

    public MinisterioService(MinisterioRepository ministerioRepository) {
        this.ministerioRepository = ministerioRepository;
    }

    @Transactional(readOnly = true)
    public List<Ministerio> listarTodos() {
        return ministerioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ministerio buscarPorId(Long id) {
        return ministerioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ministério", id));
    }

    @Transactional
    public Ministerio salvar(Ministerio ministerio) {
        return ministerioRepository.save(ministerio);
    }

    @Transactional
    public void deletar(Long id) {
        if (!ministerioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ministério", id);
        }
        ministerioRepository.deleteById(id);
    }
}
