package com.sg.auth.dto;

public record LoginResponseDTO(
        String token,
        String tipo,
        String nome,
        String nivel,
        Long idUsuario,
        boolean senhaTemporaria
) {}
