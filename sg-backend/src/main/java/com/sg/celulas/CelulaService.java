package com.sg.celulas;

import com.sg.membros.Membro;
import com.sg.membros.MembroRepository;
import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CelulaService {
    private final CelulaRepository celulaRepository;
    private final MembroRepository membroRepository;

    public CelulaService(CelulaRepository celulaRepository, MembroRepository membroRepository) {
        this.celulaRepository = celulaRepository;
        this.membroRepository = membroRepository;
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
    public Celula salvar(Celula celula) {
        // O líder é um membro já cadastrado; a coluna de texto é mantida
        // sincronizada para não quebrar a listagem nem o site público.
        if (celula.getLiderId() != null) {
            Membro lider = membroRepository.findById(celula.getLiderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Membro", celula.getLiderId()));
            celula.setLider(lider.getNome());
        }
        return celulaRepository.save(celula);
    }

    @Transactional
    public void deletar(Long id) {
        if (!celulaRepository.existsById(id)) throw new ResourceNotFoundException("Célula", id);
        celulaRepository.deleteById(id);
    }
}
