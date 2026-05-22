package com.agrosmart.magdalena.domain.entity;

import com.agrosmart.magdalena.domain.enums.EstadoSupervision;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "supervisiones_fincas", indexes = {
        @Index(name = "idx_supervision_finca", columnList = "finca_id"),
        @Index(name = "idx_supervision_tecnico", columnList = "tecnico_usuario_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupervisionFinca extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finca_id", nullable = false)
    @NotNull
    private Finca finca;

    @Column(name = "tecnico_usuario_id", nullable = false)
    private Long tecnicoUsuarioId;

    @Column(name = "tecnico_nombre", length = 200)
    private String tecnicoNombre;

    @Column(name = "tecnico_email", length = 200)
    private String tecnicoEmail;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoSupervision estado;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
