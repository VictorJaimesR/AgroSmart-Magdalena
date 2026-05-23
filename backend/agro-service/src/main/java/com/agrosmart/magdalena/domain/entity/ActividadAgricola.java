package com.agrosmart.magdalena.domain.entity;

import com.agrosmart.magdalena.domain.enums.TipoActividad;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registra una actividad agrícola realizada sobre un cultivo concreto.
 *
 * Reglas de negocio:
 *   - Toda actividad debe estar asociada a una finca y a un cultivo
 *   - El cultivo debe pertenecer a la finca indicada
 *   - Se guarda quién registró la actividad (agricultor o técnico) y cuándo
 *   - Permite trazabilidad completa del historial de labores sobre cada cultivo
 */
@Entity
@Table(name = "actividades_agricolas", indexes = {
        @Index(name = "idx_actividad_finca",   columnList = "finca_id"),
        @Index(name = "idx_actividad_cultivo",  columnList = "cultivo_id"),
        @Index(name = "idx_actividad_fecha",    columnList = "fecha_actividad"),
        @Index(name = "idx_actividad_tipo",     columnList = "tipo_actividad")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActividadAgricola extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finca_id", nullable = false)
    private Finca finca;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_actividad", nullable = false, length = 30)
    private TipoActividad tipoActividad;

    @Positive
    @Column(name = "cantidad")
    private Double cantidad;

    @Size(max = 30)
    @Column(name = "unidad", length = 30)
    private String unidad;

    /** Nombre del insumo/producto utilizado (fertilizante, pesticida, fungicida, etc.) */
    @Size(max = 150)
    @Column(name = "producto", length = 150)
    private String producto;

    @Size(max = 1000)
    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    /** ID del usuario (agricultor o técnico) que registró la actividad */
    @NotNull
    @Column(name = "registrado_por_id", nullable = false)
    private Long registradoPorId;

    @NotBlank
    @Size(max = 200)
    @Column(name = "registrado_por_nombre", nullable = false, length = 200)
    private String registradoPorNombre;

    /** Rol del registrador: AGRICULTOR o TECNICO */
    @Size(max = 20)
    @Column(name = "registrado_por_rol", length = 20)
    private String registradoPorRol;

    /** Fecha y hora real en que se realizó la actividad (puede diferir del momento de registro) */
    @NotNull
    @Column(name = "fecha_actividad", nullable = false)
    private LocalDateTime fechaActividad;
}
