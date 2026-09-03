package com.sg.tesoureiros;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class TesoureiroService {

    private final TesoureiroRepository tesoureiroRepository;

    public TesoureiroService(TesoureiroRepository tesoureiroRepository) {
        this.tesoureiroRepository = tesoureiroRepository;
    }

    public List<Tesoureiro> listarTodos() {
        return tesoureiroRepository.findAll();
    }

    public Tesoureiro buscarPorId(Long id) {
        return tesoureiroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tesoureiro", id));
    }

    public Tesoureiro salvar(Tesoureiro tesoureiro) {
        if (tesoureiro.getDataCad() == null) tesoureiro.setDataCad(LocalDate.now());
        return tesoureiroRepository.save(tesoureiro);
    }

    public Tesoureiro atualizar(Long id, Tesoureiro tesoureiro) {
        buscarPorId(id);
        tesoureiro.setId(id);
        return tesoureiroRepository.save(tesoureiro);
    }

    public void deletar(Long id) {
        if (!tesoureiroRepository.existsById(id))
            throw new ResourceNotFoundException("Tesoureiro", id);
        tesoureiroRepository.deleteById(id);
    }
}
