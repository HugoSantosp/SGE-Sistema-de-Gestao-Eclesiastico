package com.sg.celulas;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "celulas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Celula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 100)
    private String lider;

    @Column(length = 200)
    private String endereco;

    @Column(name = "dia_semana")
    private String diaSemana;

    private LocalTime horario;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(length = 255)
    private String foto;
}
