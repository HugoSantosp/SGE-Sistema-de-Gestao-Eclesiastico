package com.sg.escala.dto;

import com.sg.escala.EscalaDesignacao;

public record EscalaDesignacaoResponseDTO(
        Long id,
        Long confirmacaoId,
        String nomeIntegrante,
        String instrumento,
        Integer ordem
) {
    public static EscalaDesignacaoResponseDTO fromEntity(EscalaDesignacao d) {
        return new EscalaDesignacaoResponseDTO(
                d.getId(),
                d.getConfirmacao().getId(),
                d.getConfirmacao().getNome(),
                d.getInstrumento(),
                d.getOrdem()
        );
    }
}
