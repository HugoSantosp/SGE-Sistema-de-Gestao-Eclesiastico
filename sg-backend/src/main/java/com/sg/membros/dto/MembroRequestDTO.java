package com.sg.membros.dto;

import com.sg.shared.enums.StatusMembro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MembroRequestDTO(
        @NotBlank String nome,
        String documento, String telefone, String endereco,
        String foto, LocalDate dataNasc,
        @NotNull StatusMembro situacao,
        Long funcaoId,
        Long ministerioId,
        LocalDate dataBatismo,
        String obs
) {}
