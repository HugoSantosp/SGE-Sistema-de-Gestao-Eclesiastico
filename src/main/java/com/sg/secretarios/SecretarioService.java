package com.sg.secretarios;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class SecretarioService {

    private final SecretarioRepository secretarioRepository;

    public SecretarioService(SecretarioRepository secretarioRepository) {
        this.secretarioRepository = secretarioRepository;
    }

    public List<Secretario> listarTodos() {
        return secretarioRepository.findAll();
    }

    public Secretario buscarPorId(Long id) {
        return secretarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Secretário", id));
    }

    public Secretario salvar(Secretario secretario) {
        if (secretario.getDataCad() == null) secretario.setDataCad(LocalDate.now());
        return secretarioRepository.save(secretario);
    }

    public Secretario atualizar(Long id, Secretario secretario) {
        buscarPorId(id);
        secretario.setId(id);
        return secretarioRepository.save(secretario);
    }

    public void deletar(Long id) {
        if (!secretarioRepository.existsById(id))
            throw new ResourceNotFoundException("Secretário", id);
        secretarioRepository.deleteById(id);
    }
}
