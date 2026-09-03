package com.sg.escala;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "escalas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Escala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    /** Ministério dono da escala (ex.: Louvor, Recepção, Mídia). Null = escala genérica. */
    @Column(name = "ministerio_id")
    private Long ministerioId;

    @Column(name = "public_token", nullable = false, unique = true, length = 40)
    private String publicToken;

    @Column(name = "resultado_token", unique = true, length = 40)
    private String resultadoToken;

    @Column(nullable = false)
    @Builder.Default
    private boolean aberta = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "escala", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<EscalaData> datas = new ArrayList<>();

    @OneToMany(mappedBy = "escala", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<EscalaConfirmacao> confirmacoes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
