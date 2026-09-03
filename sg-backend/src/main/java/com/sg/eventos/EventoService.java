package com.sg.eventos;

import com.sg.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;

    public EventoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Transactional(readOnly = true)
    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Evento> listarProximos() {
        return eventoRepository.findProximos(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Evento> listarPorMesAno(int mes, int ano) {
        return eventoRepository.findByMesAno(mes, ano);
    }

    @Transactional(readOnly = true)
    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));
    }

    @Transactional
    public Evento salvar(Evento evento) {
        return eventoRepository.save(evento);
    }

    @Transactional
    public void deletar(Long id) {
        if (!eventoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Evento", id);
        }
        eventoRepository.deleteById(id);
    }
}
