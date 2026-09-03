package com.sg.ministerios;

import com.sg.membros.Membro;
import com.sg.shared.enums.PapelMinisterio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Vínculo N:N entre Ministério e Membro (modelo estilo Voluts).
 * Um membro pode participar de vários ministérios, com um papel em cada um.
 */
@Entity
@Table(name = "ministerio_membro",
        uniqueConstraints = @UniqueConstraint(name = "uk_ministerio_membro", columnNames = {"ministerio_id", "membro_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MinisterioMembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ministerio_id", nullable = false)
    @ToString.Exclude
    private Ministerio ministerio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", nullable = false)
    @ToString.Exclude
    private Membro membro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PapelMinisterio papel;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }
}
