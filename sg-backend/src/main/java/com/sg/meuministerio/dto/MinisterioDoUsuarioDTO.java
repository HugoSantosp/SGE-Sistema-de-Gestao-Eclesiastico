package com.sg.meuministerio.dto;

import com.sg.shared.enums.PapelMinisterio;

/**
 * Ministério do ponto de vista do usuário logado (app MeuMinisterio).
 * <p>
 * papel = papel real do usuário naquele ministério quando ele possui vínculo
 * (ex.: pastor que continua servindo como LIDER/MUSICO do Louvor);
 * papel = null quando o usuário não possui vínculo (ex.: pastor com acesso ampliado
 * a um ministério onde ele não serve).
 */
public record MinisterioDoUsuarioDTO(
        Long id,
        String nome,
        String descricao,
        String foto,
        PapelMinisterio papel
) {}
