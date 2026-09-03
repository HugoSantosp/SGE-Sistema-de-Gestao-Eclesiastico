package com.sg.financeiro;

import com.sg.shared.enums.FrequenciaPagamento;
import com.sg.shared.enums.StatusConta;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contas_pagar")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContaPagar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Column(name = "fornecedor_id")
    private Long fornecedorId;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_cad", nullable = false)
    private LocalDate dataCad;

    @Column(nullable = false)
    private LocalDate vencimento;

    @Column(name = "usuario_cad_id")
    private Long usuarioCadId;

    @Column(name = "usuario_baixa_id")
    private Long usuarioBaixaId;

    @Column(name = "data_baixa")
    private LocalDate dataBaixa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrequenciaPagamento frequencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusConta status;

    @Column(length = 150)
    private String arquivo;
}
