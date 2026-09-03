package com.sg.auth.dto;

public record PerfilResponseDTO(
        Long id,
        String nome,
        String email,
        String documento,
        String foto,
        String nivel,
        Long idPessoa
) {}
