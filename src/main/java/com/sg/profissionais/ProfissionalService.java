package com.sg.profissionais;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProfissionalService {
    private final ProfissionalRepository profissionalRepository;

    public ProfissionalService(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    @Transactional(readOnly = true)
    public List<Profissional> listarTodos() {
        return profissionalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Profissional> listarPublicos() {
        return profissionalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Profissional buscarPorId(Long id) {
        return profissionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", id));
    }

    @Transactional
    public Profissional salvar(Profissional profissional) { return profissionalRepository.save(profissional); }

    @Transactional
    public void deletar(Long id) {
        if (!profissionalRepository.existsById(id)) throw new ResourceNotFoundException("Profissional", id);
        profissionalRepository.deleteById(id);
    }
}
