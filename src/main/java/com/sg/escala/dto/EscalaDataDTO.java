package com.sg.escala.dto;

import com.sg.escala.EscalaData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record EscalaDataDTO(
        Long id,
        @NotBlank String nomeEvento,
        @NotNull LocalDate data,
        @NotNull LocalTime horario,
        @NotBlank String local
) {
    public static EscalaDataDTO fromEntity(EscalaData d) {
        return new EscalaDataDTO(
                d.getId(),
                d.getNomeEvento(),
                d.getData(),
                d.getHorario(),
                d.getLocal()
        );
    }
}
