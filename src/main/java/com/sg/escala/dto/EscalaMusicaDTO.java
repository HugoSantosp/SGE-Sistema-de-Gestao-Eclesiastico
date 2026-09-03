package com.sg.escala.dto;

import com.sg.escala.EscalaMusica;
import jakarta.validation.constraints.NotBlank;

public record EscalaMusicaDTO(
        Long id,
        @NotBlank String nome,
        String artista,
        String link,
        Integer ordem
) {
    public static EscalaMusicaDTO fromEntity(EscalaMusica m) {
        return new EscalaMusicaDTO(
                m.getId(),
                m.getNome(),
                m.getArtista(),
                m.getLink(),
                m.getOrdem()
        );
    }
}
