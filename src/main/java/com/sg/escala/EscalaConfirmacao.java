package com.sg.escala;

import com.sg.membros.Membro;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "escala_confirmacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EscalaConfirmacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escala_id", nullable = false)
    @ToString.Exclude
    private Escala escala;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id")
    @ToString.Exclude
    private Membro membro;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String celular;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
        name = "confirmacao_data",
        joinColumns = @JoinColumn(name = "confirmacao_id"),
        inverseJoinColumns = @JoinColumn(name = "data_id")
    )
    @Builder.Default
    @ToString.Exclude
    private List<EscalaData> datas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
