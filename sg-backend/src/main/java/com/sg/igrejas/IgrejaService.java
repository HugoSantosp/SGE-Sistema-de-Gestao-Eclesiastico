package com.sg.igrejas;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class IgrejaService {

    private final IgrejaRepository igrejaRepository;

    public IgrejaService(IgrejaRepository igrejaRepository) {
        this.igrejaRepository = igrejaRepository;
    }

    public List<Igreja> listarTodas() { return igrejaRepository.findAll(); }

    public Igreja buscarPorId(Long id) {
        return igrejaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Igreja", id));
    }

    public Igreja salvar(Igreja igreja) {
        if (igreja.getDataCad() == null) igreja.setDataCad(LocalDate.now());
        return igrejaRepository.save(igreja);
    }

    public Igreja atualizar(Long id, Igreja igreja) {
        buscarPorId(id);
        igreja.setId(id);
        return igrejaRepository.save(igreja);
    }

    public void deletar(Long id) {
        if (!igrejaRepository.existsById(id))
            throw new ResourceNotFoundException("Igreja", id);
        igrejaRepository.deleteById(id);
    }

    public long contarTotal() { return igrejaRepository.count(); }
}
