package com.sg.shared.enums;

/**
 * Papéis que um membro pode ter dentro de um ministério.
 * <p>
 * Além de LIDER (gestão) e INTEGRANTE (participação geral), existem papéis
 * funcionais que refletem a função da pessoa no ministério (estilo Voluts):
 * músico, vocalista, backing vocal, técnico, operador, intercessor etc.
 * <p>
 * Regras de permissão:
 * - LIDER: gerencia membros e monta escalas do ministério.
 * - Qualquer papel: permite confirmar disponibilidade e ver escalas do ministério.
 */
public enum PapelMinisterio {
    LIDER,
    INTEGRANTE,
    MUSICO,
    VOCALISTA,
    BACKING_VOCAL,
    TECNICO,
    OPERADOR,
    INTERCESSOR,
    RECEPCAO,
    FACILITADOR
}
