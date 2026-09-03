package com.sg.meuministerio.dto;

import com.sg.shared.enums.PapelMinisterio;

/**
 * Membro dentro de um ministério (vínculo ministerio_membro) com o papel atribuído.
 */
public record MembroMinisterioDTO(
        Long vinculoId,
        Long membroId,
        String nome,
        String documento,
        String telefone,
        String foto,
        PapelMinisterio papel
) {}
