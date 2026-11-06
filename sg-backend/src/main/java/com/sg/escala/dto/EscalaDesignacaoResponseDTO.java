package com.sg.escala.dto;

import com.sg.escala.EscalaDesignacao;

public record EscalaDesignacaoResponseDTO(
        Long id,
        Long confirmacaoId,
        String nomeIntegrante,
        String instrumento,
        Integer ordem,
        String fotoIntegrante
) {
    /**
     * @param fotoIntegrante URL assinada da foto do integrante (vazia quando não há foto).
     *                       A assinatura é feita pelo serviço, nunca pelo DTO.
     */
    public static EscalaDesignacaoResponseDTO fromEntity(EscalaDesignacao d, String fotoIntegrante) {
        return new EscalaDesignacaoResponseDTO(
                d.getId(),
                d.getConfirmacao().getId(),
                d.getConfirmacao().getNome(),
                d.getInstrumento(),
                d.getOrdem(),
                fotoIntegrante != null ? fotoIntegrante : ""
        );
    }
}
