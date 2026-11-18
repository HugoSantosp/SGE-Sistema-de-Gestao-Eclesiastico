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

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(length = 255)
    private String observacao;

    @Column(name = "justificativa_cancelamento", length = 255)
    private String justificativaCancelamento;

    @Column(name = "usuario_cancelamento_id")
    private Long usuarioCancelamentoId;

    @Column(name = "data_cancelamento")
    private LocalDate dataCancelamento;

    public String statusExibicao(BigDecimal totalPago) {
        if (status == null) return "-";
        boolean emAberto = status == StatusConta.PENDENTE || status == StatusConta.VENCIDA;
        if (emAberto && totalPago != null && totalPago.compareTo(BigDecimal.ZERO) > 0) {
            return "PARCIALMENTE_PAGA";
        }
        return status.name();
    }
}
