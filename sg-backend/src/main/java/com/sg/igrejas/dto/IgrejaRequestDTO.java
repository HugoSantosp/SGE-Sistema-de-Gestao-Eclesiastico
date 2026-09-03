package com.sg.igrejas.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record IgrejaRequestDTO(
        @NotBlank String nome,
        @NotBlank String telefone,
        String endereco, String obs, String foto,
        @NotBlank String matriz,
        Long pastorId
) {}
