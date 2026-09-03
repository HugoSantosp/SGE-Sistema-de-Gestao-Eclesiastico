package com.sg.cargos;

import com.sg.shared.exceptions.BusinessException;
import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CargoService {

    private final CargoRepository cargoRepository;

    public CargoService(CargoRepository cargoRepository) {
        this.cargoRepository = cargoRepository;
    }

    public List<Cargo> listarTodos() { return cargoRepository.findAll(); }

    public Cargo buscarPorId(Long id) {
        return cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo", id));
    }

    public Cargo salvar(Cargo cargo) {
        if (cargoRepository.existsByNome(cargo.getNome())) {
            throw new BusinessException("Já existe um cargo com este nome");
        }
        return cargoRepository.save(cargo);
    }

    public Cargo atualizar(Long id, Cargo cargo) {
        buscarPorId(id);
        cargo.setId(id);
        return cargoRepository.save(cargo);
    }

    public void deletar(Long id) {
        if (!cargoRepository.existsById(id))
            throw new ResourceNotFoundException("Cargo", id);
        cargoRepository.deleteById(id);
    }
}
