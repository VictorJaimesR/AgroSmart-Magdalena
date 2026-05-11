package com.agrosmart.magdalena.domain.entity;

import com.agrosmart.magdalena.domain.enums.EstadoCultivo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * Representa un cultivo dentro de una parcela.
 *
 * Reglas de negocio:
 *   - Un cultivo pertenece a exactamente una parcela
 *   - El área utilizada no debe exceder el área disponible de la parcela
 *   - El ciclo de vida del cultivo está definido por EstadoCultivo
 *   - Soporta soft delete
 */
@Entity
@Table(name = "cultivos", indexes = {
        @Index(name = "idx_cultivo_parcela", columnList = "parcela_id"),
        @Index(name = "idx_cultivo_estado", columnList = "estado"),
        @Index(name = "idx_cultivo_activo", columnList = "activo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cultivo extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 100)
    @Column(length = 100)
    private String variedad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcela_id", nullable = false)
    private Parcela parcela;

    @NotNull
    @Column(name = "fecha_siembra", nullable = false)
    private LocalDate fechaSiembra;

    @Column(name = "fecha_cosecha_estimada")
    private LocalDate fechaCosechaEstimada;

    @Column(name = "fecha_cosecha_real")
    private LocalDate fechaCosechaReal;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCultivo estado = EstadoCultivo.PLANIFICADO;

    @Positive
    @Column(name = "area_utilizada")
    private Double areaUtilizada;

    @Size(max = 500)
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "rendimiento_esperado")
    private Double rendimientoEsperado;

    @Column(name = "rendimiento_real")
    private Double rendimientoReal;

    @Size(max = 30)
    @Column(name = "unidad_rendimiento", length = 30)
    @Builder.Default
    private String unidadRendimiento = "toneladas/hectárea";

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
