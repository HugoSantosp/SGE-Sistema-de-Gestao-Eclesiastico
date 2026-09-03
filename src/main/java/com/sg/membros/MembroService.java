package com.sg.membros;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class MembroService {

    private final MembroRepository membroRepository;

    public MembroService(MembroRepository membroRepository) {
        this.membroRepository = membroRepository;
    }

    public List<Membro> listarTodos() {
        return membroRepository.findAll();
    }

    public Membro buscarPorId(Long id) {
        return membroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membro", id));
    }

    public Membro salvar(Membro membro) {
        if (membro.getDataCad() == null) membro.setDataCad(LocalDate.now());
        return membroRepository.save(membro);
    }

    public Membro atualizar(Long id, Membro membro) {
        buscarPorId(id);
        membro.setId(id);
        return membroRepository.save(membro);
    }

    public void deletar(Long id) {
        if (!membroRepository.existsById(id))
            throw new ResourceNotFoundException("Membro", id);
        membroRepository.deleteById(id);
    }

    public long contarAtivos() {
        return membroRepository.countBySituacao(com.sg.shared.enums.StatusMembro.ATIVO);
    }

    public long contarInativos() {
        return membroRepository.countBySituacao(com.sg.shared.enums.StatusMembro.INATIVO);
    }

    public long contarTotal() {
        return membroRepository.count();
    }
}
