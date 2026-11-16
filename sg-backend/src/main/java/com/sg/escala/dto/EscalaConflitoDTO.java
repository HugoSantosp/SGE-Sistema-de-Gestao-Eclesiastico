package com.sg.escala.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Uma data já confirmada pelo membro em outro ministério que colide com a data
 * que ele está tentando confirmar agora (mesma data e mesmo horário).
 */
public record EscalaConflitoDTO(
        LocalDate data,
        LocalTime horario,
        String nomeEvento,
        Long escalaId,
        String escalaTitulo,
        Long ministerioId,
        String ministerioNome
) {}
