package com.sg.escala.dto;

import com.sg.escala.Escala;

import java.time.LocalDateTime;

public record EscalaResponseDTO(
        Long id,
        String titulo,
        Long ministerioId,
        String publicToken,
        String resultadoToken,
        boolean aberta,
        int datasCount,
        int confirmacoesCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static EscalaResponseDTO fromEntity(Escala e, int datasCount, int confirmacoesCount) {
        return new EscalaResponseDTO(
                e.getId(),
                e.getTitulo(),
                e.getMinisterioId(),
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
