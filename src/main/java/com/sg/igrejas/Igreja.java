package com.sg.igrejas;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "igrejas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Igreja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(length = 150)
    private String endereco;

    @Column(columnDefinition = "TEXT")
    private String obs;

    @Column(length = 150)
    private String foto;

    @Column(nullable = false, length = 5)
    private String matriz;

    @Column(name = "data_cad", nullable = false)
    private LocalDate dataCad;

    @Column(name = "pastor_id")
    private Long pastorId;
}
