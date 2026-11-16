package com.sg.escala;

import com.sg.escala.dto.EscalaConflitoDTO;
import com.sg.ministerios.Ministerio;
import com.sg.ministerios.MinisterioRepository;
import com.sg.shared.exceptions.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Impede que o mesmo membro confirme a mesma data e o mesmo horário em dois
 * ministérios diferentes.
 *
 * <p>Regra aprovada:
 * <ul>
 *   <li>conflito = mesma pessoa + mesma data + mesmo horário, em escalas de ministérios diferentes;</li>
 *   <li>escalas genéricas ({@code ministerioId == null}) ficam fora da regra;</li>
 *   <li>escalas do mesmo ministério não geram conflito entre si.</li>
 * </ul>
 *
 * <p>Usado pelos dois caminhos de confirmação: o logado
 * ({@code MeuMinisterioService}) e o link público com token ({@code EscalaService}).
 */
@Service
public class EscalaConflitoService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final EscalaConfirmacaoRepository confirmacaoRepository;
    private final MinisterioRepository ministerioRepository;

    public EscalaConflitoService(EscalaConfirmacaoRepository confirmacaoRepository,
                                 MinisterioRepository ministerioRepository) {
        this.confirmacaoRepository = confirmacaoRepository;
        this.ministerioRepository = ministerioRepository;
    }

    /**
     * Datas em conflito para o membro nas datas informadas. Lista vazia quando não há
     * conflito, quando a escala é genérica ou quando o membro não foi identificado.
     */
    @Transactional(readOnly = true)
    public List<EscalaConflitoDTO> conflitos(Long membroId, Escala escala, Collection<EscalaData> datas) {
        if (membroId == null || escala == null || escala.getId() == null
                || escala.getMinisterioId() == null || datas == null || datas.isEmpty()) {
            return List.of();
        }

        Set<String> chaves = new HashSet<>();
        for (EscalaData data : datas) {
            chaves.add(chave(data.getData(), data.getHorario()));
        }
        if (chaves.isEmpty()) {
            return List.of();
        }

        Map<Long, String> nomesMinisterio = new HashMap<>();
        List<EscalaConflitoDTO> encontrados = new ArrayList<>();

        for (EscalaConfirmacao confirmacao
                : confirmacaoRepository.buscarConfirmacoesEmOutrasEscalas(membroId, escala.getId())) {
            Long ministerioId = confirmacao.getEscala().getMinisterioId();
            if (ministerioId == null || ministerioId.equals(escala.getMinisterioId())) {
                continue;
            }
            for (EscalaData data : confirmacao.getDatas()) {
                if (!chaves.contains(chave(data.getData(), data.getHorario()))) {
                    continue;
                }
                encontrados.add(new EscalaConflitoDTO(
                        data.getData(),
                        data.getHorario(),
                        data.getNomeEvento(),
                        confirmacao.getEscala().getId(),
                        confirmacao.getEscala().getTitulo(),
                        ministerioId,
                        nomesMinisterio.computeIfAbsent(ministerioId, this::nomeMinisterio)
                ));
            }
        }

        encontrados.sort(Comparator
                .comparing(EscalaConflitoDTO::data)
                .thenComparing(EscalaConflitoDTO::horario));
        return encontrados;
    }

    /** Lança {@link BusinessException} (HTTP 422) quando existe ao menos um conflito. */
    @Transactional(readOnly = true)
    public void validar(Long membroId, Escala escala, Collection<EscalaData> datas) {
        List<EscalaConflitoDTO> conflitos = conflitos(membroId, escala, datas);
        if (conflitos.isEmpty()) {
            return;
        }
        throw new BusinessException(mensagem(conflitos));
    }

    private String nomeMinisterio(Long ministerioId) {
        return ministerioRepository.findById(ministerioId)
                .map(Ministerio::getNome)
                .orElse("outro ministério");
    }

    private String mensagem(List<EscalaConflitoDTO> conflitos) {
        StringBuilder texto = new StringBuilder(
                "Você já está disponível em outro ministério nesta data e horário: ");
        for (int i = 0; i < conflitos.size(); i++) {
            EscalaConflitoDTO conflito = conflitos.get(i);
            if (i > 0) {
                texto.append("; ");
            }
            texto.append(conflito.data().format(FORMATO_DATA))
                    .append(" às ")
                    .append(conflito.horario().format(FORMATO_HORA))
                    .append(" — ")
                    .append(conflito.ministerioNome());
        }
        texto.append(". Desmarque a data em um dos ministérios para continuar.");
        return texto.toString();
    }

    private static String chave(LocalDate data, LocalTime horario) {
        return data + "T" + (horario == null ? "" : horario);
    }
}
