package com.sg.financeiro;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Saldo inicial configurável. Cada alteração gera um novo registro: o histórico
 * é preservado e o saldo vigente é o de maior data de referência.
 */
@Entity
@Table(name = "saldos_iniciais")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SaldoInicial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @Column(length = 255)
    private String observacao;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "data_cad", nullable = false)
    private LocalDate dataCad;
}
