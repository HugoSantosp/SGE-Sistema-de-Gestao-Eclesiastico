package com.sg.fornecedores.dto;

import jakarta.validation.constraints.NotBlank;

public record FornecedorRequestDTO(
        @NotBlank String nome,
        String telefone, String endereco, String email, String produto
) {}
