package com.sg.escala.dto;

import com.sg.escala.Escala;
import com.sg.escala.EscalaData;
import com.sg.escala.EscalaDesignacao;
import com.sg.escala.EscalaMusica;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record EscalaDetalhadaResponseDTO(
        Long id,
        String titulo,
        Long ministerioId,
        String publicToken,
        String resultadoToken,
        boolean aberta,
        List<DataDetalhadaDTO> datas,
        List<EscalaConfirmacaoResponseDTO> confirmacoes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static EscalaDetalhadaResponseDTO fromEntity(
            Escala escala,
            List<EscalaData> datas,
            Map<Long, List<EscalaDesignacao>> designacoesPorData,
            Map<Long, List<EscalaMusica>> musicasPorData
    ) {
        var confirmacoesDTO = escala.getConfirmacoes().stream().map(c ->
                EscalaConfirmacaoResponseDTO.fromEntity(c,
                        c.getDatas().stream().map(EscalaData::getId).toList())
        ).toList();

        return fromEntity(escala, datas, designacoesPorData, musicasPorData, confirmacoesDTO);
    }

    /**
     * Versão pública: NÃO expõe as confirmações (nome, email, celular) de quem já confirmou.
     * Usada pelo endpoint de confirmação (link público) — só precisa das datas.
     */
    public static EscalaDetalhadaResponseDTO fromEntityPublica(
            Escala escala,
            List<EscalaData> datas,
            Map<Long, List<EscalaDesignacao>> designacoesPorData,
            Map<Long, List<EscalaMusica>> musicasPorData
    ) {
        return fromEntity(escala, datas, designacoesPorData, musicasPorData, List.of());
    }

    private static EscalaDetalhadaResponseDTO fromEntity(
            Escala escala,
            List<EscalaData> datas,
            Map<Long, List<EscalaDesignacao>> designacoesPorData,
            Map<Long, List<EscalaMusica>> musicasPorData,
            List<EscalaConfirmacaoResponseDTO> confirmacoesDTO
    ) {
        var datasDTO = datas.stream().map(d -> DataDetalhadaDTO.fromEntity(
                d,
                designacoesPorData.getOrDefault(d.getId(), List.of()),
                musicasPorData.getOrDefault(d.getId(), List.of())
        )).toList();

        return new EscalaDetalhadaResponseDTO(
                escala.getId(),
                escala.getTitulo(),
                escala.getMinisterioId(),
                escala.getPublicToken(),
                escala.getResultadoToken(),
                escala.isAberta(),
                datasDTO,
                confirmacoesDTO,
                escala.getCreatedAt(),
                escala.getUpdatedAt()
        );
    }

    public record DataDetalhadaDTO(
            Long id,
            String nomeEvento,
            String data,
            String horario,
            String local,
            List<EscalaDesignacaoResponseDTO> designacoes,
            List<EscalaMusicaDTO> musicas
    ) {
        static DataDetalhadaDTO fromEntity(
                EscalaData d,
                List<EscalaDesignacao> designacoes,
                List<EscalaMusica> musicas
        ) {
            return new DataDetalhadaDTO(
                    d.getId(),
                    d.getNomeEvento(),
                    d.getData().toString(),
                    d.getHorario().toString(),
                    d.getLocal(),
                    designacoes.stream().map(EscalaDesignacaoResponseDTO::fromEntity).toList(),
                    musicas.stream().map(EscalaMusicaDTO::fromEntity).toList()
            );
        }
    }
}
