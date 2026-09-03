package com.sg.bispos.dto;

import java.time.LocalDate;

public record BispoResponseDTO(
        Long id,
        String nome,
        String email,
        String documento,
        String telefone,
        String endereco,
        String foto,
        LocalDate dataCad,
        LocalDate dataNasc
) {}
