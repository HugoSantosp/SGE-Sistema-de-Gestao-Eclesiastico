package com.sg.escala;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "escala_musicas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EscalaMusica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escala_data_id", nullable = false)
    @ToString.Exclude
    private EscalaData escalaData;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 200)
    private String artista;

    @Column(length = 500)
    private String link;

    @Column(nullable = false)
    @Builder.Default
    private Integer ordem = 0;
}
