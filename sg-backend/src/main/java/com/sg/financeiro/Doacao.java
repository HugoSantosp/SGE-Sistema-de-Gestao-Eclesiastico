package com.sg.financeiro;

import com.sg.shared.enums.FormaPagamento;
import com.sg.shared.enums.StatusDoacao;
import com.sg.shared.enums.TipoDoacao;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "doacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Doacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDoacao tipo;

    /** Preenchido em DÍZIMO e PRIMÍCIA; nulo em OFERTA */
    @Column(name = "membro_id")
    private Long membroId;

    /** Nome do doador: nome do membro nos tipos vinculados, texto livre na oferta */
    @Column(name = "nome_doador", length = 150)
    private String nomeDoador;

    @Column(name = "data_doacao", nullable = false)
    private LocalDate dataDoacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private FormaPagamento formaPagamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(length = 255)
    private String descricao;

    @Column(name = "data_cad", nullable = false)
    private LocalDate dataCad;

    @Column(name = "categoria_id")
    private Long categoriaId;

    /** Doação cancelada não integra os cálculos financeiros. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ATIVA'")
    @Builder.Default
    private StatusDoacao status = StatusDoacao.ATIVA;

    @Column(name = "justificativa_cancelamento", length = 255)
    private String justificativaCancelamento;

    @Column(name = "usuario_cad_id")
    private Long usuarioCadId;
}
