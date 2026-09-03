package com.sg.tarefas;

import com.sg.shared.enums.StatusTarefa;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tarefas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 50)
    private String titulo;

    @Column(length = 100)
    private String descricao;

    @Column(name = "hora_tarefa", nullable = false)
    private LocalTime horaTarefa;

    @Column(name = "data_tarefa", nullable = false)
    private LocalDate dataTarefa;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_tarefa", nullable = false, length = 15)
    private StatusTarefa statusTarefa;
}
