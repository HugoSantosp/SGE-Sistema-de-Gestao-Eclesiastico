package com.sg.fornecedores;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fornecedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String telefone;

    @Column(length = 50)
    private String endereco;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String produto;
}
