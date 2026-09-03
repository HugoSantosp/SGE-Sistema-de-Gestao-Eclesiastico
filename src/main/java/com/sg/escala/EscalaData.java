package com.sg.escala;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "escala_datas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EscalaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escala_id", nullable = false)
    @ToString.Exclude
    private Escala escala;

    @Column(name = "nome_evento", nullable = false, length = 120)
    private String nomeEvento;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime horario;

    @Column(nullable = false, length = 120)
    private String local;

    @ManyToMany(mappedBy = "datas")
    @Builder.Default
    @ToString.Exclude
    private List<EscalaConfirmacao> confirmacoes = new ArrayList<>();

    @OneToMany(mappedBy = "escalaData", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<EscalaMusica> musicas = new ArrayList<>();
}
