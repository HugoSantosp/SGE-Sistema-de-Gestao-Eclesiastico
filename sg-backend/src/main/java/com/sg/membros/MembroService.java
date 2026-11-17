package com.sg.membros;

import com.sg.cargos.CargoService;
import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class MembroService {

    private final MembroRepository membroRepository;
    private final CargoService cargoService;

    public MembroService(MembroRepository membroRepository, CargoService cargoService) {
        this.membroRepository = membroRepository;
        this.cargoService = cargoService;
    }

    public List<Membro> listarTodos() {
        return membroRepository.findAll();
    }

    /**
     * Lista membros filtrando por situação. Quando {@code situacao} é nulo,
     * devolve todos — mantendo o comportamento anterior de {@code GET /api/membros}.
     */
    public List<Membro> listar(com.sg.shared.enums.StatusMembro situacao) {
        if (situacao == null) return membroRepository.findAll();
        return membroRepository.findBySituacao(situacao);
    }

    public Membro buscarPorId(Long id) {
        return membroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membro", id));
    }

    public Membro salvar(Membro membro) {
        validarCargo(membro.getCargoId());
        if (membro.getDataCad() == null) membro.setDataCad(LocalDate.now());
        return membroRepository.save(membro);
    }

    public Membro atualizar(Long id, Membro membro) {
        buscarPorId(id);
        validarCargo(membro.getCargoId());
        membro.setId(id);
        return membroRepository.save(membro);
    }

    /**
     * Cargo é opcional, mas quando informado precisa existir na tabela de cargos.
     * Sem isso, um id inválido ficaria gravado e a listagem mostraria "-".
     */
    private void validarCargo(Long cargoId) {
        if (cargoId != null) cargoService.buscarPorId(cargoId);
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
