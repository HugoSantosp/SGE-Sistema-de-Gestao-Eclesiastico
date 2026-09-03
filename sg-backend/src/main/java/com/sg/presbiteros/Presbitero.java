package com.sg.presbiteros;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "presbiteros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Presbitero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(length = 20)
    private String documento;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String endereco;

    @Column(length = 150)
    private String foto;

    @Column(name = "data_cad")
    private LocalDate dataCad;

    @Column(name = "data_nasc")
    private LocalDate dataNasc;

    @Column(columnDefinition = "TEXT")
    private String obs;
}
