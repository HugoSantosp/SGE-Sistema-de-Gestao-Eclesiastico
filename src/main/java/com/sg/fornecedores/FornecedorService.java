package com.sg.fornecedores;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    public List<Fornecedor> listarTodos() {
        return fornecedorRepository.findAll();
    }

    public Fornecedor buscarPorId(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", id));
    }

    public Fornecedor salvar(Fornecedor fornecedor) {
        return fornecedorRepository.save(fornecedor);
    }

    public Fornecedor atualizar(Long id, Fornecedor fornecedor) {
        buscarPorId(id);
        fornecedor.setId(id);
        return fornecedorRepository.save(fornecedor);
    }

    public void deletar(Long id) {
        if (!fornecedorRepository.existsById(id))
            throw new ResourceNotFoundException("Fornecedor", id);
        fornecedorRepository.deleteById(id);
    }
}
