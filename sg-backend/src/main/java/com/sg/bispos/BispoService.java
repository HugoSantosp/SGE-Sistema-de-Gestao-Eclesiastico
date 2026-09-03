package com.sg.bispos;

import com.sg.shared.exceptions.BusinessException;
import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BispoService {

    private final BispoRepository bispoRepository;

    public BispoService(BispoRepository bispoRepository) {
        this.bispoRepository = bispoRepository;
    }

    public List<Bispo> listarTodos() {
        return bispoRepository.findAll();
    }

    public Bispo buscarPorId(Long id) {
        return bispoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bispo", id));
    }

    public Bispo salvar(Bispo bispo) {
        if (bispo.getDataCad() == null) {
            bispo.setDataCad(LocalDate.now());
        }
        return bispoRepository.save(bispo);
    }

    public Bispo atualizar(Long id, Bispo bispo) {
        buscarPorId(id);
        bispo.setId(id);
        return bispoRepository.save(bispo);
    }

    public void deletar(Long id) {
        if (!bispoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bispo", id);
        }
        bispoRepository.deleteById(id);
    }
}
