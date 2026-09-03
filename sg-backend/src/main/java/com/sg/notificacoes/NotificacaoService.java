package com.sg.notificacoes;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    public List<Notificacao> listarTodas() { return notificacaoRepository.findAll(); }

    public List<Notificacao> listarDoDia() {
        return notificacaoRepository.buscarPorData(LocalDate.now());
    }

    public Notificacao buscarPorId(Long id) {
        return notificacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação", id));
    }

    public Notificacao salvar(Notificacao notificacao) {
        return notificacaoRepository.save(notificacao);
    }

    public void deletar(Long id) {
        if (!notificacaoRepository.existsById(id))
            throw new ResourceNotFoundException("Notificação", id);
        notificacaoRepository.deleteById(id);
    }
}
