package com.sg.escala.dto;

import com.sg.escala.Escala;

import java.time.LocalDateTime;

public record EscalaResponseDTO(
        Long id,
        String titulo,
        Long ministerioId,
        String nomeMinisterio,
        String publicToken,
        String resultadoToken,
        boolean aberta,
        int datasCount,
        int confirmacoesCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static EscalaResponseDTO fromEntity(
            Escala e,
            int datasCount,
            int confirmacoesCount
    ) {
        return fromEntity(e, datasCount, confirmacoesCount, null);
    }

    public static EscalaResponseDTO fromEntity(Escala e, int datasCount, int confirmacoesCount, String nomeMinisterio) {
        return new EscalaResponseDTO(
                e.getId(),
                e.getTitulo(),
                e.getMinisterioId(),
                nomeMinisterio,
                e.getPublicToken(),
                e.getResultadoToken(),
                e.isAberta(),
                datasCount,
                confirmacoesCount,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }


}
