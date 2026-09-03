package com.sg.escala.dto;

import com.sg.escala.EscalaConfirmacao;

import java.time.LocalDateTime;
import java.util.List;

public record EscalaConfirmacaoResponseDTO(
        Long id,
        Long membroId,
        String membroNome,
        String nome,
        String email,
        String celular,
        List<Long> dataIds,
        LocalDateTime createdAt
) {
    public static EscalaConfirmacaoResponseDTO fromEntity(EscalaConfirmacao c, List<Long> dataIds) {
        return new EscalaConfirmacaoResponseDTO(
                c.getId(),
                c.getMembro() != null ? c.getMembro().getId() : null,
                c.getMembro() != null ? c.getMembro().getNome() : null,
                c.getNome(),
                c.getEmail(),
                c.getCelular(),
                dataIds,
                c.getCreatedAt()
        );
    }
}
