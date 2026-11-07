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

    /**
     * Busca por ID quando o valor é numérico e por nome caso contrário.
     *
     * O formulário genérico do SGE carrega a edição em GET /api/config/{id}, e o
     * endpoint antigo interpretava esse número como nome, devolvendo 404 e deixando
     * o formulário vazio. Nada no frontend consome a busca por nome, então unificar
     * aqui preserva o contrato e corrige a edição.
     */
    public Configuracao buscarPorIdOuNome(String idOuNome) {
        if (idOuNome != null && idOuNome.matches("\\d+")) {
            Long id = Long.valueOf(idOuNome);
            return configuracaoRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Configuração", id));
        }
        return buscarPorNome(idOuNome);
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
