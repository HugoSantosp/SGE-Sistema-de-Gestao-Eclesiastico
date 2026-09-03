package com.sg.escala;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "escala_designacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EscalaDesignacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escala_data_id", nullable = false)
    @ToString.Exclude
    private EscalaData escalaData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmacao_id", nullable = false)
    @ToString.Exclude
    private EscalaConfirmacao confirmacao;

    @Column(nullable = false, length = 60)
    private String instrumento;

    @Column(nullable = false)
    @Builder.Default
    private Integer ordem = 0;
}
