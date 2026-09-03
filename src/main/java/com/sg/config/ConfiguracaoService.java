package com.sg.config;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ConfiguracaoService {

    private final ConfiguracaoRepository configuracaoRepository;

    public ConfiguracaoService(ConfiguracaoRepository configuracaoRepository) {
        this.configuracaoRepository = configuracaoRepository;
    }

    public List<Configuracao> listarTodas() { return configuracaoRepository.findAll(); }

    public Configuracao buscarPorNome(String nome) {
        return configuracaoRepository.findByNome(nome)
                .orElseThrow(() -> new ResourceNotFoundException("Configuração: " + nome));
    }

    public Configuracao salvar(Configuracao configuracao) {
        return configuracaoRepository.save(configuracao);
    }

    public void deletar(Long id) {
        if (!configuracaoRepository.existsById(id))
            throw new ResourceNotFoundException("Configuração", id);
        configuracaoRepository.deleteById(id);
    }
}
