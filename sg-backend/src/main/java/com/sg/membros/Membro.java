package com.sg.membros;

import com.sg.shared.enums.StatusMembro;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "membros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Membro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 11)
    private StatusMembro situacao;

    @Column(name = "funcao_id")
    private Long funcaoId;

    @Column(name = "ministerio_id")
    private Long ministerioId;

    @Column(name = "data_batismo")
    private LocalDate dataBatismo;

    @Column(columnDefinition = "TEXT")
    private String obs;
}
