package com.agrosmart.magdalena.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una finca (unidad productiva) de un productor.
 *
 * Reglas de negocio:
 *   - Una finca pertenece a exactamente un productor
 *   - Una finca tiene una ubicación geográfica obligatoria
 *   - Una finca puede tener múltiples parcelas/lotes
 *   - Soporta soft delete mediante el campo 'activo'
 */
@Entity
@Table(name = "fincas", indexes = {
        @Index(name = "idx_finca_productor", columnList = "productor_id"),
        @Index(name = "idx_finca_activo", columnList = "activo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Finca extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productor_id", nullable = false)
    private Productor productor;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private UbicacionGeografica ubicacion;

    @NotNull
    @Positive
    @Column(name = "area_total", nullable = false)
    private Double areaTotal;

    @Size(max = 20)
    @Column(name = "unidad_area", length = 20)
    @Builder.Default
    private String unidadArea = "hectáreas";

    @Size(max = 500)
    @Column(length = 500)
    private String descripcion;

    @OneToMany(mappedBy = "finca", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Parcela> parcelas = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
