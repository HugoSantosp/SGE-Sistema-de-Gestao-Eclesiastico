package com.sg.usuario.dto;

import com.sg.shared.enums.NivelAcesso;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String documento,
        String email,
        NivelAcesso nivel,
        String foto
) {}
