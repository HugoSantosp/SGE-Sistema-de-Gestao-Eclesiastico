package com.sg.financeiro;

import com.sg.shared.enums.FormaPagamento;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pagamento (integral ou parcial) vinculado a uma conta a pagar.
 * O histórico é preservado: nunca é sobrescrito nem excluído.
 */
@Entity
@Table(name = "pagamentos_despesa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PagamentoDespesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "conta_pagar_id", nullable = false)
    private Long contaPagarId;

    @Column(name = "valor_pago", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPago;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private FormaPagamento formaPagamento;

    @Column(length = 255)
    private String observacao;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "data_cad", nullable = false)
    private LocalDate dataCad;
}
