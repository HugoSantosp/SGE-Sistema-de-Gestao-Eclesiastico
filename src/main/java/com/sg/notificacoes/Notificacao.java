package com.sg.notificacoes;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "notificacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 100)
    private String atividade;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(name = "data_not", nullable = false)
    private LocalDate dataNot;

    @Column(name = "status_not", nullable = false, length = 50)
    private String statusNot;
}
